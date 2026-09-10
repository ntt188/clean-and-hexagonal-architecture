package com.example.cleanorders.application.order.port.out;

import com.example.cleanorders.domain.order.entity.Customer;
import com.example.cleanorders.domain.order.entity.Order;

public interface NotificationGateway {
    void notifyOrderConfirmed(Customer customer, Order order);
}
