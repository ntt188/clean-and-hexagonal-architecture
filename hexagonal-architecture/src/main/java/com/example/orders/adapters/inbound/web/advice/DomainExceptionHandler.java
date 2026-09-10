package com.example.orders.adapters.inbound.web.advice;

import com.example.orders.adapters.inbound.web.response.ErrorResponse;
import com.example.orders.domain.order.exception.DomainException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * NOI DUY NHAT dich ngoai le cua domain sang ma HTTP.
 *
 * Truoc day viec nay nam ngay trong {@code OrderController}, khien controller vua
 * dinh tuyen vua dich loi, va moi controller moi lai phai lap lai. Tach ra day thi
 * controller chi con mot viec, va moi endpoint deu duoc xu ly loi giong nhau.
 */
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ErrorResponse> handleDomainException(DomainException e) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse("REJECTED", e.getMessage()));
    }
}
