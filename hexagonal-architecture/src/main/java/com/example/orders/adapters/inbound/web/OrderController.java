package com.example.orders.adapters.inbound.web;

import com.example.orders.adapters.inbound.web.mapper.OrderWebMapper;
import com.example.orders.adapters.inbound.web.request.PlaceOrderRequest;
import com.example.orders.adapters.inbound.web.response.PlaceOrderResponse;
import com.example.orders.application.order.dto.PlaceOrderResult;
import com.example.orders.application.order.port.in.PlaceOrderUseCase;
import com.example.orders.domain.order.exception.DomainException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * INBOUND ADAPTER: chỉ dịch HTTP sang Command rồi gọi cổng vào.
 * Không có một dòng nghiệp vụ nào ở đây.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final OrderWebMapper mapper;

    public OrderController(PlaceOrderUseCase placeOrderUseCase, OrderWebMapper mapper) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        PlaceOrderResult result = placeOrderUseCase.placeOrder(mapper.toCommand(request));
        PlaceOrderResponse body = mapper.toResponse(result);

        return result.accepted()
                ? ResponseEntity.status(HttpStatus.CREATED).body(body)
                : ResponseEntity.unprocessableEntity().body(body);
    }

    /** Lỗi domain lọt ra ngoài được dịch thành mã HTTP tại đây — biên của hệ thống. */
    @ExceptionHandler(DomainException.class)
    public ResponseEntity<PlaceOrderResponse> handleDomainException(DomainException e) {
        return ResponseEntity.badRequest()
                .body(new PlaceOrderResponse(null, "REJECTED", null, null, e.getMessage()));
    }
}
