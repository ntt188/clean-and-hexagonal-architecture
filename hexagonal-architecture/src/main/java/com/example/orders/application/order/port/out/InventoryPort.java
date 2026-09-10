package com.example.orders.application.order.port.out;

import com.example.orders.domain.order.entity.Money;

import java.util.Optional;

/** OUTBOUND PORT: lõi cần hỏi hệ thống kho (một service khác). */
public interface InventoryPort {

    record ProductInfo(String productId, String name, Money unitPrice, int available) {}

    Optional<ProductInfo> findProduct(String productId);

    void reserve(String productId, int quantity);
}
