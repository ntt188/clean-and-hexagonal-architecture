package com.example.orders.domain.order.service;

import com.example.orders.domain.order.entity.Money;
import com.example.orders.domain.order.entity.Order;

import java.math.BigDecimal;

/**
 * Domain Service: chứa quy tắc nghiệp vụ KHÔNG thuộc riêng một entity nào.
 * Ở đây là chính sách chiết khấu, nó cần biết cả đơn hàng lẫn ngưỡng của doanh nghiệp.
 */
public class OrderPricingService {

    private static final Money DISCOUNT_THRESHOLD = Money.of("500.00");
    private static final BigDecimal DISCOUNT_RATE = new BigDecimal("0.05");

    /** Đơn trên 500 được giảm 5%. */
    public Money priceOf(Order order) {
        Money subtotal = order.subtotal();
        if (subtotal.isGreaterThan(DISCOUNT_THRESHOLD)) {
            return subtotal.minus(subtotal.percentage(DISCOUNT_RATE));
        }
        return subtotal;
    }
}
