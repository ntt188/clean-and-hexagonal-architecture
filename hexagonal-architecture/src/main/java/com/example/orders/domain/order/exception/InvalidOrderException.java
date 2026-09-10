package com.example.orders.domain.order.exception;

/** Đơn hàng vi phạm quy tắc cấu tạo (rỗng, quá số dòng, số lượng sai...). */
public class InvalidOrderException extends DomainException {
    public InvalidOrderException(String message) {
        super(message);
    }
}
