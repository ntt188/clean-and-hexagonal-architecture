package com.example.cleanorders.application.order.port.out;

import com.example.cleanorders.domain.order.entity.Customer;
import com.example.cleanorders.domain.order.entity.CustomerId;

import java.util.Optional;

public interface CustomerGateway {
    Optional<Customer> findById(CustomerId customerId);
}
