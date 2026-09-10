package com.example.orders.application.order.port.out;

import com.example.orders.domain.order.entity.Product;

import java.util.Optional;

/**
 * OUTBOUND PORT: lõi cần hỏi hệ thống kho (một service khác).
 * Trả về entity domain {@code Product}, không tự định nghĩa kiểu dữ liệu riêng —
 * nhất quán với {@code CustomerRepository} trả về {@code Customer}.
 */
public interface InventoryPort {

    Optional<Product> findProduct(String productId);

    void reserve(String productId, int quantity);
}
