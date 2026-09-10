package com.example.cleanorders.application.order.port.out;

import com.example.cleanorders.domain.order.entity.Product;

import java.util.Optional;

/**
 * GATEWAY: lõi cần hỏi hệ thống kho.
 * Trả về entity domain {@code Product}, không tự định nghĩa kiểu dữ liệu riêng —
 * nhất quán với {@code CustomerGateway} trả về {@code Customer}.
 */
public interface InventoryGateway {

    Optional<Product> findProduct(String productId);

    void reserve(String productId, int quantity);
}
