package com.example.orders.application.order.usecase;

import com.example.orders.application.order.dto.PlaceOrderCommand;
import com.example.orders.application.order.dto.PlaceOrderResult;
import com.example.orders.application.order.port.in.PlaceOrderUseCase;
import com.example.orders.application.order.port.out.CustomerRepository;
import com.example.orders.application.order.port.out.InventoryPort;
import com.example.orders.application.order.port.out.NotificationPort;
import com.example.orders.application.order.port.out.OrderRepository;
import com.example.orders.domain.order.entity.Customer;
import com.example.orders.domain.order.entity.CustomerId;
import com.example.orders.domain.order.entity.Money;
import com.example.orders.domain.order.entity.Order;
import com.example.orders.domain.order.entity.OrderId;
import com.example.orders.domain.order.entity.OrderItem;
import com.example.orders.domain.order.entity.Product;
import com.example.orders.domain.order.exception.DomainException;
import com.example.orders.domain.order.service.OrderPricingService;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Cài đặt cổng vào. KHÔNG có annotation của Spring — bean được khai báo trong
 * {@code config/UseCaseConfig}. Nhờ vậy tầng application hoàn toàn độc lập framework.
 */
public class PlaceOrderService implements PlaceOrderUseCase {

    private final CustomerRepository customerRepository;
    private final InventoryPort inventoryPort;
    private final OrderRepository orderRepository;
    private final NotificationPort notificationPort;
    private final OrderPricingService pricingService;
    private final Clock clock;

    public PlaceOrderService(CustomerRepository customerRepository,
                             InventoryPort inventoryPort,
                             OrderRepository orderRepository,
                             NotificationPort notificationPort,
                             OrderPricingService pricingService,
                             Clock clock) {
        this.customerRepository = customerRepository;
        this.inventoryPort = inventoryPort;
        this.orderRepository = orderRepository;
        this.notificationPort = notificationPort;
        this.pricingService = pricingService;
        this.clock = clock;
    }

    @Override
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
        try {
            // (1) Nạp khách hàng qua cổng ra
            Optional<Customer> found = customerRepository.findById(CustomerId.of(command.customerId()));
            if (found.isEmpty()) {
                return PlaceOrderResult.rejected("Khong tim thay khach hang: " + command.customerId());
            }
            Customer customer = found.get();

            // (2) Hỏi kho; Product tự trả lời còn hàng không và tự dựng dòng hàng
            List<OrderItem> items = new ArrayList<>();
            for (PlaceOrderCommand.Item line : command.items()) {
                Optional<Product> foundProduct = inventoryPort.findProduct(line.productId());
                if (foundProduct.isEmpty()) {
                    return PlaceOrderResult.rejected("San pham khong ton tai: " + line.productId());
                }
                Product product = foundProduct.get();
                if (!product.hasStockFor(line.quantity())) {
                    return PlaceOrderResult.rejected("Khong du hang: " + product.name());
                }
                items.add(product.orderLine(line.quantity()));
            }

            // (3) Domain tạo đơn
            Order order = Order.place(OrderId.newId(), customer.id(), items, clock.instant());

            // (4) Domain service tính giá cuối (có chiết khấu)
            Money finalTotal = pricingService.priceOf(order);
            order.applyPricing(finalTotal);

            // (5) Quy tắc hạn mức tín dụng do entity Customer quyết định
            if (!customer.canAfford(order.totalAmount())) {
                order.reject("Vuot han muc tin dung");
                orderRepository.save(order);
                return PlaceOrderResult.rejected("Don hang " + order.totalAmount()
                        + " vuot han muc " + customer.creditLimit());
            }

            // (6) Xác nhận và lưu TRƯỚC
            order.confirm();
            Order saved = orderRepository.save(order);

            // (7) Giữ hàng SAU khi đã ghi đơn.
            // Kho là hệ thống ngoài, không tham gia giao dịch của ta nên không thể rollback.
            // Đặt nó sau bước ghi CSDL để một lỗi ghi không bao giờ để lại hàng bị giữ mồ côi;
            // ngược lại, nếu giữ hàng hỏng thì ngoại lệ ném ra sẽ cuốn theo cả bản ghi đơn.
            for (OrderItem item : saved.items()) {
                inventoryPort.reserve(item.productId(), item.quantity());
            }

            // (8) Thông báo cho khách — adapter sẽ hoãn tới sau khi giao dịch commit
            notificationPort.notifyOrderConfirmed(customer, saved);

            // (9) TRẢ VỀ kết quả cho adapter
            return PlaceOrderResult.accepted(saved.id().value(), saved.status().name(),
                    customer.name(), saved.totalAmount().toString());

        } catch (DomainException e) {
            return PlaceOrderResult.rejected(e.getMessage());
        }
    }
}
