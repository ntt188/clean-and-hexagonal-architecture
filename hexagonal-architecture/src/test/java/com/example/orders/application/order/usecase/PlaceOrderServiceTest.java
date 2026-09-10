package com.example.orders.application.order.usecase;

import com.example.orders.application.order.dto.PlaceOrderCommand;
import com.example.orders.application.order.dto.PlaceOrderResult;
import com.example.orders.application.order.port.out.CustomerRepository;
import com.example.orders.application.order.port.out.InventoryPort;
import com.example.orders.application.order.port.out.NotificationPort;
import com.example.orders.application.order.port.out.OrderRepository;
import com.example.orders.domain.order.entity.Customer;
import com.example.orders.domain.order.entity.CustomerId;
import com.example.orders.domain.order.entity.Money;
import com.example.orders.domain.order.entity.Order;
import com.example.orders.domain.order.entity.OrderId;
import com.example.orders.domain.order.exception.InvalidOrderException;
import com.example.orders.domain.order.service.OrderPricingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Lõi nghiệp vụ được test bằng JUnit thuần với adapter giả — KHÔNG khởi động Spring,
 * KHÔNG chạm database. Đây chính là lợi ích lớn nhất của kiến trúc ports & adapters.
 */
class PlaceOrderServiceTest {

    // --- Adapter giả cắm vào các cổng ra ---

    static class FakeCustomerRepository implements CustomerRepository {
        private final Map<String, Customer> table = new LinkedHashMap<>();
        FakeCustomerRepository() {
            table.put("C-01", new Customer(CustomerId.of("C-01"), "Nguyen Van A", "a@example.com", Money.of("1000.00")));
            table.put("C-02", new Customer(CustomerId.of("C-02"), "Tran Thi B", "b@example.com", Money.of("50.00")));
        }
        @Override public Optional<Customer> findById(CustomerId id) {
            return Optional.ofNullable(table.get(id.value()));
        }
    }

    static class FakeInventory implements InventoryPort {
        private final Map<String, ProductInfo> catalog = new LinkedHashMap<>();
        final List<String> reserved = new ArrayList<>();
        FakeInventory() {
            catalog.put("P-01", new ProductInfo("P-01", "Ban phim co", Money.of("120.00"), 10));
            catalog.put("P-02", new ProductInfo("P-02", "Chuot khong day", Money.of("45.50"), 3));
            catalog.put("P-03", new ProductInfo("P-03", "Man hinh 27 inch", Money.of("320.00"), 0));
        }
        @Override public Optional<ProductInfo> findProduct(String productId) {
            return Optional.ofNullable(catalog.get(productId));
        }
        @Override public void reserve(String productId, int quantity) {
            reserved.add(productId + "x" + quantity);
        }
    }

    static class FakeOrderRepository implements OrderRepository {
        final List<Order> saved = new ArrayList<>();
        boolean failOnSave = false;
        @Override public Order save(Order order) {
            if (failOnSave) throw new IllegalStateException("CSDL hong");
            saved.add(order);
            return order;
        }
        @Override public Optional<Order> findById(OrderId id) {
            return saved.stream().filter(o -> o.id().equals(id)).findFirst();
        }
    }

    static class FakeNotification implements NotificationPort {
        int sent = 0;
        @Override public void notifyOrderConfirmed(Customer customer, Order order) { sent++; }
    }

    // --- Lắp ráp thủ công, không cần container ---

    private final FakeCustomerRepository customers = new FakeCustomerRepository();
    private final FakeInventory inventory = new FakeInventory();
    private final FakeOrderRepository orders = new FakeOrderRepository();
    private final FakeNotification notifications = new FakeNotification();

    private final PlaceOrderService service = new PlaceOrderService(
            customers, inventory, orders, notifications, new OrderPricingService(),
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    private PlaceOrderCommand cart(String customerId, String productId, int qty) {
        return new PlaceOrderCommand(customerId, List.of(new PlaceOrderCommand.Item(productId, qty)));
    }

    @Test
    @DisplayName("Kich ban 1: dat hang hop le -> CONFIRMED, giu hang, gui thong bao")
    void placeOrderSuccessfully() {
        PlaceOrderResult result = service.placeOrder(cart("C-01", "P-01", 2));

        assertTrue(result.accepted());
        assertEquals("CONFIRMED", result.status());
        assertEquals("240.00", result.totalAmount());
        assertEquals("Nguyen Van A", result.customerName());
        assertEquals(List.of("P-01x2"), inventory.reserved);
        assertEquals(1, notifications.sent);
        assertEquals(1, orders.saved.size());
    }

    @Test
    @DisplayName("Kich ban 2: het hang -> tu choi, khong luu, khong thong bao")
    void rejectWhenOutOfStock() {
        PlaceOrderResult result = service.placeOrder(cart("C-01", "P-03", 1));

        assertFalse(result.accepted());
        assertEquals("Khong du hang: Man hinh 27 inch", result.reason());
        assertTrue(orders.saved.isEmpty());
        assertEquals(0, notifications.sent);
    }

    @Test
    @DisplayName("Kich ban 3: vuot han muc tin dung -> REJECTED nhung van duoc luu lai")
    void rejectWhenOverCreditLimit() {
        PlaceOrderResult result = service.placeOrder(cart("C-02", "P-01", 1));

        assertFalse(result.accepted());
        assertTrue(result.reason().contains("vuot han muc"));
        assertEquals(1, orders.saved.size());
        assertEquals("REJECTED", orders.saved.get(0).status().name());
        assertEquals(0, notifications.sent);
    }

    @Test
    @DisplayName("Kich ban 4: khach hang khong ton tai -> tu choi")
    void rejectWhenCustomerNotFound() {
        PlaceOrderResult result = service.placeOrder(cart("C-99", "P-01", 1));

        assertFalse(result.accepted());
        assertTrue(result.reason().contains("Khong tim thay khach hang"));
    }

    @Test
    @DisplayName("Kich ban 5: don tren 500 duoc chiet khau 5% (domain service)")
    void applyDiscountForLargeOrder() {
        PlaceOrderResult result = service.placeOrder(cart("C-01", "P-01", 5));

        assertTrue(result.accepted());
        assertEquals("570.00", result.totalAmount());   // 600.00 - 5%
    }

    @Test
    @DisplayName("Tinh nguyen tu: ghi CSDL hong -> khong giu hang, khong gui thong bao")
    void doNotReserveOrNotifyWhenSaveFails() {
        orders.failOnSave = true;

        // Ngoai le duoc nem ra ngoai de decorator giao dich rollback toan bo.
        assertThrows(IllegalStateException.class, () -> service.placeOrder(cart("C-01", "P-01", 2)));

        assertTrue(inventory.reserved.isEmpty(), "Khong duoc giu hang khi don chua ghi duoc");
        assertEquals(0, notifications.sent, "Khong duoc gui email khi don chua ghi duoc");
    }

    @Test
    @DisplayName("Command tu kiem tra: gio hang rong bi chan ngay tai bien")
    void rejectEmptyCart() {
        assertThrows(InvalidOrderException.class,
                () -> new PlaceOrderCommand("C-01", List.of()));
    }
}
