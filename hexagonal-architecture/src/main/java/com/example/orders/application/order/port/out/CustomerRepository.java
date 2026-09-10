package com.example.orders.application.order.port.out;

import com.example.orders.domain.order.entity.Customer;
import com.example.orders.domain.order.entity.CustomerId;

import java.util.Optional;

/** OUTBOUND PORT: lõi cần đọc thông tin khách hàng. */
public interface CustomerRepository {
    Optional<Customer> findById(CustomerId customerId);
}
