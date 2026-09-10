package com.example.orders.application.order.port.out;

import com.example.orders.domain.order.entity.Order;
import com.example.orders.domain.order.entity.OrderId;

import java.util.Optional;

/**
 * OUTBOUND PORT (driven): lõi cần lưu/đọc đơn hàng.
 * Interface do tầng application SỞ HỮU; JPA phải cài đặt theo, không phải ngược lại.
 */
public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(OrderId orderId);
}
