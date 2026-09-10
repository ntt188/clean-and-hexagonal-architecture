package com.example.cleanorders.application.order.port.out;

import com.example.cleanorders.domain.order.entity.Order;
import com.example.cleanorders.domain.order.entity.OrderId;

import java.util.Optional;

/** GATEWAY: interface do tầng use case sở hữu, tầng adapter phải cài đặt theo. */
public interface OrderGateway {
    Order save(Order order);
    Optional<Order> findById(OrderId orderId);
}
