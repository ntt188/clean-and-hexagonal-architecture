package com.example.cleanorders.domain.order.entity;

import com.example.cleanorders.domain.order.exception.InvalidOrderException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root. Đây là POJO thuần: không @Entity, không annotation của JPA/Spring,
 * nên có thể unit test mà không cần khởi động framework nào.
 */
public class Order {

    public static final int MAX_ITEMS = 10;

    private final OrderId id;
    private final CustomerId customerId;
    private final List<OrderItem> items;
    private final Instant placedAt;
    private OrderStatus status;
    private Money totalAmount;
    private String rejectReason;

    private Order(OrderId id, CustomerId customerId, List<OrderItem> items, Instant placedAt,
                  OrderStatus status, Money totalAmount, String rejectReason) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.placedAt = placedAt;
        this.status = status;
        this.totalAmount = totalAmount;
        this.rejectReason = rejectReason;
    }

    /** Factory tạo đơn mới, kiểm tra quy tắc cấu tạo. */
    public static Order place(OrderId id, CustomerId customerId, List<OrderItem> items, Instant placedAt) {
        if (items == null || items.isEmpty())
            throw new InvalidOrderException("Don hang phai co it nhat 1 dong hang");
        if (items.size() > MAX_ITEMS)
            throw new InvalidOrderException("Don hang toi da " + MAX_ITEMS + " dong hang");
        Order order = new Order(id, customerId, items, placedAt, OrderStatus.NEW, Money.zero(), null);
        order.totalAmount = order.subtotal();
        return order;
    }

    /** Dựng lại đơn từ dữ liệu đã lưu (dùng bởi persistence mapper). */
    public static Order restore(OrderId id, CustomerId customerId, List<OrderItem> items, Instant placedAt,
                                OrderStatus status, Money totalAmount, String rejectReason) {
        return new Order(id, customerId, items, placedAt, status, totalAmount, rejectReason);
    }

    /** Tổng tiền hàng trước khuyến mãi. */
    public Money subtotal() {
        Money sum = Money.zero();
        for (OrderItem item : items) sum = sum.plus(item.subtotal());
        return sum;
    }

    /** Ghi nhận giá cuối do OrderPricingService tính. Giá cuối không được lớn hơn tiền hàng. */
    public void applyPricing(Money finalTotal) {
        if (status != OrderStatus.NEW)
            throw new InvalidOrderException("Chi dinh gia duoc cho don o trang thai NEW");
        if (finalTotal.isGreaterThan(subtotal()))
            throw new InvalidOrderException("Gia cuoi khong duoc lon hon tien hang");
        this.totalAmount = finalTotal;
    }

    public void confirm() {
        if (status != OrderStatus.NEW)
            throw new InvalidOrderException("Chi don o trang thai NEW moi duoc xac nhan");
        this.status = OrderStatus.CONFIRMED;
    }

    public void reject(String reason) {
        this.status = OrderStatus.REJECTED;
        this.rejectReason = reason;
    }

    public OrderId id()            { return id; }
    public CustomerId customerId() { return customerId; }
    public List<OrderItem> items() { return Collections.unmodifiableList(items); }
    public Instant placedAt()      { return placedAt; }
    public OrderStatus status()    { return status; }
    public Money totalAmount()     { return totalAmount; }
    public String rejectReason()   { return rejectReason; }
}
