package com.example.orders.domain.order.entity;

import com.example.orders.domain.order.exception.InvalidOrderException;

/** Value Object: một dòng hàng. */
public record OrderItem(String productId, String productName, Money unitPrice, int quantity) {

    public OrderItem {
        if (productId == null || productId.isBlank())
            throw new InvalidOrderException("Ma san pham khong duoc rong");
        if (quantity <= 0)
            throw new InvalidOrderException("So luong phai lon hon 0 (san pham: " + productId + ")");
    }

    /** Quy tắc: thành tiền = đơn giá x số lượng. */
    public Money subtotal() {
        return unitPrice.times(quantity);
    }
}
