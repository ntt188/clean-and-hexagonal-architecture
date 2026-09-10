package com.example.cleanorders.application.order.usecase;

import com.example.cleanorders.adapters.inbound.web.presenter.OrderViewModel;
import com.example.cleanorders.adapters.inbound.web.presenter.PlaceOrderPresenter;
import com.example.cleanorders.application.order.dto.PlaceOrderRequestModel;
import com.example.cleanorders.application.order.port.out.CustomerGateway;
import com.example.cleanorders.application.order.port.out.InventoryGateway;
import com.example.cleanorders.application.order.port.out.NotificationGateway;
import com.example.cleanorders.application.order.port.out.OrderGateway;
import com.example.cleanorders.domain.order.entity.Customer;
import com.example.cleanorders.domain.order.entity.CustomerId;
import com.example.cleanorders.domain.order.entity.Money;
import com.example.cleanorders.domain.order.entity.Order;
import com.example.cleanorders.domain.order.entity.OrderId;
import com.example.cleanorders.domain.order.exception.InvalidOrderException;
import com.example.cleanorders.domain.order.service.OrderPricingService;
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
 * Test lõi nghiệp vụ bằng JUnit thuần: gateway giả + presenter thật, không cần Spring.
 * Vì use case đẩy kết quả sang Output Boundary nên khẳng định được thực hiện trên View Model.
 */
class PlaceOrderInteractorTest {

    static class FakeCustomerGateway implements CustomerGateway {
        private final Map<String, Customer> table = new LinkedHashMap<>();
        FakeCustomerGateway() {
            table.put("C-01", new Customer(CustomerId.of("C-01"), "Nguyen Van A", "a@example.com", Money.of("1000.00")));
            table.put("C-02", new Customer(CustomerId.of("C-02"), "Tran Thi B", "b@example.com", Money.of("50.00")));
        }
        @Override public Optional<Customer> findById(CustomerId id) {
            return Optional.ofNullable(table.get(id.value()));
        }
    }

    static class FakeInventoryGateway implements InventoryGateway {
        private final Map<String, ProductInfo> catalog = new LinkedHashMap<>();
        final List<String> reserved = new ArrayList<>();
        FakeInventoryGateway() {
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

    static class FakeOrderGateway implements OrderGateway {
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

    static class FakeNotificationGateway implements NotificationGateway {
        int sent = 0;
        @Override public void notifyOrderConfirmed(Customer customer, Order order) { sent++; }
    }

    private final FakeCustomerGateway customers = new FakeCustomerGateway();
    private final FakeInventoryGateway inventory = new FakeInventoryGateway();
    private final FakeOrderGateway orders = new FakeOrderGateway();
    private final FakeNotificationGateway notifications = new FakeNotificationGateway();
    private final PlaceOrderPresenter presenter = new PlaceOrderPresenter();

    private final PlaceOrderInteractor interactor = new PlaceOrderInteractor(
            customers, inventory, orders, notifications, new OrderPricingService(), presenter,
            Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC));

    private PlaceOrderRequestModel cart(String customerId, String productId, int qty) {
        return new PlaceOrderRequestModel(customerId, List.of(new PlaceOrderRequestModel.Item(productId, qty)));
    }

    @Test
    @DisplayName("Kich ban 1: dat hang hop le -> Presenter nhan View Model thanh cong")
    void placeOrderSuccessfully() {
        interactor.execute(cart("C-01", "P-01", 2));

        OrderViewModel vm = presenter.viewModel();
        assertTrue(vm.success());
        assertEquals(201, vm.httpStatus());
        assertEquals("CONFIRMED", vm.status());
        assertEquals("240.00", vm.totalAmount());
        assertEquals("Nguyen Van A", vm.customerName());
        assertEquals(List.of("P-01x2"), inventory.reserved);
        assertEquals(1, notifications.sent);
    }

    @Test
    @DisplayName("Kich ban 2: het hang -> Presenter nhan that bai, khong luu")
    void rejectWhenOutOfStock() {
        interactor.execute(cart("C-01", "P-03", 1));

        OrderViewModel vm = presenter.viewModel();
        assertFalse(vm.success());
        assertEquals(422, vm.httpStatus());
        assertEquals("Khong du hang: Man hinh 27 inch", vm.message());
        assertTrue(orders.saved.isEmpty());
        assertEquals(0, notifications.sent);
    }

    @Test
    @DisplayName("Kich ban 3: vuot han muc -> REJECTED nhung van duoc luu lai")
    void rejectWhenOverCreditLimit() {
        interactor.execute(cart("C-02", "P-01", 1));

        OrderViewModel vm = presenter.viewModel();
        assertFalse(vm.success());
        assertTrue(vm.message().contains("vuot han muc"));
        assertEquals(1, orders.saved.size());
        assertEquals("REJECTED", orders.saved.get(0).status().name());
        assertEquals(0, notifications.sent);
    }

    @Test
    @DisplayName("Kich ban 4: khach hang khong ton tai -> tu choi")
    void rejectWhenCustomerNotFound() {
        interactor.execute(cart("C-99", "P-01", 1));

        assertFalse(presenter.viewModel().success());
        assertTrue(presenter.viewModel().message().contains("Khong tim thay khach hang"));
    }

    @Test
    @DisplayName("Kich ban 5: don tren 500 duoc chiet khau 5% (domain service)")
    void applyDiscountForLargeOrder() {
        interactor.execute(cart("C-01", "P-01", 5));

        assertTrue(presenter.viewModel().success());
        assertEquals("570.00", presenter.viewModel().totalAmount());   // 600.00 - 5%
    }

    @Test
    @DisplayName("Tinh nguyen tu: ghi CSDL hong -> khong giu hang, khong gui thong bao")
    void doNotReserveOrNotifyWhenSaveFails() {
        orders.failOnSave = true;

        // Interactor chi bat DomainException; loi ha tang phai loi ra de giao dich rollback.
        assertThrows(IllegalStateException.class, () -> interactor.execute(cart("C-01", "P-01", 2)));

        assertTrue(inventory.reserved.isEmpty(), "Khong duoc giu hang khi don chua ghi duoc");
        assertEquals(0, notifications.sent, "Khong duoc gui email khi don chua ghi duoc");
    }

    @Test
    @DisplayName("Request Model tu kiem tra: gio hang rong bi chan ngay tai bien")
    void rejectEmptyCart() {
        assertThrows(InvalidOrderException.class,
                () -> new PlaceOrderRequestModel("C-01", List.of()));
    }
}
