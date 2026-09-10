package com.example.orders.application.order.port.out;

import com.example.orders.domain.order.entity.Customer;
import com.example.orders.domain.order.entity.Order;

/** OUTBOUND PORT: lõi cần báo cho khách. Email hay SMS là việc của adapter. */
public interface NotificationPort {
    void notifyOrderConfirmed(Customer customer, Order order);
}
