package com.example.cleanorders.application.order.port.out;

import com.example.cleanorders.domain.order.entity.Money;

import java.util.Optional;

public interface InventoryGateway {

    record ProductInfo(String productId, String name, Money unitPrice, int available) {}

    Optional<ProductInfo> findProduct(String productId);

    void reserve(String productId, int quantity);
}
