package com.example.cleanorders.adapters.inbound.web;

import com.example.cleanorders.adapters.inbound.web.mapper.OrderWebMapper;
import com.example.cleanorders.adapters.inbound.web.presenter.OrderViewModel;
import com.example.cleanorders.adapters.inbound.web.presenter.PlaceOrderPresenter;
import com.example.cleanorders.adapters.inbound.web.request.PlaceOrderRequest;
import com.example.cleanorders.adapters.inbound.web.response.PlaceOrderResponse;
import com.example.cleanorders.application.order.port.in.PlaceOrderInputBoundary;
import com.example.cleanorders.domain.order.exception.DomainException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CONTROLLER: dịch HTTP thành Request Model rồi gọi Input Boundary.
 * KHÁC HEXAGONAL: nó không nhận giá trị trả về từ use case, mà đọc View Model
 * do Presenter đã chuẩn bị sẵn. Cả mã HTTP cũng do Presenter quyết định.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final PlaceOrderInputBoundary placeOrderInputBoundary;
    private final PlaceOrderPresenter presenter;
    private final OrderWebMapper mapper;

    public OrderController(PlaceOrderInputBoundary placeOrderInputBoundary,
                           PlaceOrderPresenter presenter,
                           OrderWebMapper mapper) {
        this.placeOrderInputBoundary = placeOrderInputBoundary;
        this.presenter = presenter;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        placeOrderInputBoundary.execute(mapper.toRequestModel(request));

        OrderViewModel viewModel = presenter.viewModel();
        return ResponseEntity.status(viewModel.httpStatus()).body(mapper.toResponse(viewModel));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<PlaceOrderResponse> handleDomainException(DomainException e) {
        return ResponseEntity.badRequest()
                .body(new PlaceOrderResponse(null, "REJECTED", null, null, e.getMessage()));
    }
}
