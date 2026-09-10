package com.example.cleanorders.application.order.usecase;

import com.example.cleanorders.application.order.dto.PlaceOrderRequestModel;
import com.example.cleanorders.application.order.dto.PlaceOrderResponseModel;
import com.example.cleanorders.application.order.port.in.PlaceOrderInputBoundary;
import com.example.cleanorders.application.order.port.out.CustomerGateway;
import com.example.cleanorders.application.order.port.out.InventoryGateway;
import com.example.cleanorders.application.order.port.out.NotificationGateway;
import com.example.cleanorders.application.order.port.out.OrderGateway;
import com.example.cleanorders.application.order.port.out.PlaceOrderOutputBoundary;
import com.example.cleanorders.domain.order.entity.Customer;
import com.example.cleanorders.domain.order.entity.CustomerId;
import com.example.cleanorders.domain.order.entity.Money;
import com.example.cleanorders.domain.order.entity.Order;
import com.example.cleanorders.domain.order.entity.OrderId;
import com.example.cleanorders.domain.order.entity.OrderItem;
import com.example.cleanorders.domain.order.entity.Product;
import com.example.cleanorders.domain.order.exception.DomainException;
import com.example.cleanorders.domain.order.service.OrderPricingService;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * INTERACTOR = Application Business Rules.
 * Điều phối kịch bản, hỏi Entity/Domain Service về quy tắc, nói chuyện với
 * thế giới ngoài qua Gateway, và ĐẨY kết quả sang Output Boundary.
 * Không có annotation Spring — bean được khai báo ở config/UseCaseConfig.
 */
public class PlaceOrderInteractor implements PlaceOrderInputBoundary {

    private final CustomerGateway customerGateway;
    private final InventoryGateway inventoryGateway;
    private final OrderGateway orderGateway;
    private final NotificationGateway notificationGateway;
    private final OrderPricingService pricingService;
    private final PlaceOrderOutputBoundary outputBoundary;
    private final Clock clock;

    public PlaceOrderInteractor(CustomerGateway customerGateway,
                                InventoryGateway inventoryGateway,
                                OrderGateway orderGateway,
                                NotificationGateway notificationGateway,
                                OrderPricingService pricingService,
                                PlaceOrderOutputBoundary outputBoundary,
                                Clock clock) {
        this.customerGateway = customerGateway;
        this.inventoryGateway = inventoryGateway;
        this.orderGateway = orderGateway;
        this.notificationGateway = notificationGateway;
        this.pricingService = pricingService;
        this.outputBoundary = outputBoundary;
        this.clock = clock;
    }

    @Override
    public void execute(PlaceOrderRequestModel requestModel) {
        try {
            // (1) Nạp khách hàng qua Gateway
            Optional<Customer> found = customerGateway.findById(CustomerId.of(requestModel.customerId()));
            if (found.isEmpty()) {
                outputBoundary.presentFailure("Khong tim thay khach hang: " + requestModel.customerId());
                return;
            }
            Customer customer = found.get();

            // (2) Hỏi kho; Product tự trả lời còn hàng không và tự dựng dòng hàng
            List<OrderItem> items = new ArrayList<>();
            for (PlaceOrderRequestModel.Item line : requestModel.items()) {
                Optional<Product> foundProduct = inventoryGateway.findProduct(line.productId());
                if (foundProduct.isEmpty()) {
                    outputBoundary.presentFailure("San pham khong ton tai: " + line.productId());
                    return;
                }
                Product product = foundProduct.get();
                if (!product.hasStockFor(line.quantity())) {
                    outputBoundary.presentFailure("Khong du hang: " + product.name());
                    return;
                }
                items.add(product.orderLine(line.quantity()));
            }

            // (3) Entity tạo đơn
            Order order = Order.place(OrderId.newId(), customer.id(), items, clock.instant());

            // (4) Domain Service tính giá cuối
            Money finalTotal = pricingService.priceOf(order);
            order.applyPricing(finalTotal);

            // (5) Entity Customer quyết định quy tắc hạn mức
            if (!customer.canAfford(order.totalAmount())) {
                order.reject("Vuot han muc tin dung");
                orderGateway.save(order);
                outputBoundary.presentFailure("Don hang " + order.totalAmount()
                        + " vuot han muc " + customer.creditLimit());
                return;
            }

            // (6) Xác nhận và lưu TRƯỚC
            order.confirm();
            Order saved = orderGateway.save(order);

            // (7) Giữ hàng SAU khi đã ghi đơn.
            // Kho là hệ thống ngoài, không tham gia giao dịch của ta nên không thể rollback.
            // Đặt nó sau bước ghi CSDL để một lỗi ghi không bao giờ để lại hàng bị giữ mồ côi;
            // ngược lại, nếu giữ hàng hỏng thì ngoại lệ ném ra sẽ cuốn theo cả bản ghi đơn.
            for (OrderItem item : saved.items()) {
                inventoryGateway.reserve(item.productId(), item.quantity());
            }

            // (8) Thông báo — adapter sẽ hoãn tới sau khi giao dịch commit
            notificationGateway.notifyOrderConfirmed(customer, saved);

            // (9) ĐẨY kết quả sang Output Boundary — KHÔNG return
            outputBoundary.presentSuccess(new PlaceOrderResponseModel(
                    saved.id().value(), saved.status().name(),
                    customer.name(), saved.totalAmount().toString()));

        } catch (DomainException e) {
            outputBoundary.presentFailure(e.getMessage());
        }
    }
}
