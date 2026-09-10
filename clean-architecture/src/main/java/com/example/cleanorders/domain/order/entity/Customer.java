package com.example.cleanorders.domain.order.entity;

/** Entity khách hàng, mang quy tắc hạn mức tín dụng. */
public record Customer(CustomerId id, String name, String email, Money creditLimit) {

    /** Quy tắc: tổng tiền đơn không được vượt hạn mức. */
    public boolean canAfford(Money total) {
        return !total.isGreaterThan(creditLimit);
    }
}
