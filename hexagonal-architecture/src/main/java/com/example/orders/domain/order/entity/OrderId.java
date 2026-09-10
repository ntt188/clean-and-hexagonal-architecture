package com.example.orders.domain.order.entity;

import com.example.orders.domain.order.exception.DomainException;

import java.util.UUID;

/** Value Object định danh đơn hàng — tránh dùng String trần (primitive obsession). */
public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank())
            throw new DomainException("OrderId khong duoc rong");
    }

    public static OrderId newId()          { return new OrderId(UUID.randomUUID().toString()); }
    public static OrderId of(String value) { return new OrderId(value); }

    @Override public String toString() { return value; }
}
