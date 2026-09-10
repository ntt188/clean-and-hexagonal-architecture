package com.example.cleanorders.domain.order.entity;

import com.example.cleanorders.domain.order.exception.DomainException;

/** Value Object định danh khách hàng. */
public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank())
            throw new DomainException("CustomerId khong duoc rong");
    }

    public static CustomerId of(String value) { return new CustomerId(value); }

    @Override public String toString() { return value; }
}
