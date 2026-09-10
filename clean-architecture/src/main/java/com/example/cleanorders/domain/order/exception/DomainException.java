package com.example.cleanorders.domain.order.exception;

/** Lỗi gốc của tầng domain. Không kế thừa gì của Spring. */
public class DomainException extends RuntimeException {
    public DomainException(String message) {
        super(message);
    }
}
