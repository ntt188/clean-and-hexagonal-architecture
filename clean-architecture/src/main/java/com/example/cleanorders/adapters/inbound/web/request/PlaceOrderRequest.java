package com.example.cleanorders.adapters.inbound.web.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PlaceOrderRequest(
        @NotBlank(message = "customerId khong duoc rong") String customerId,
        @NotEmpty(message = "items khong duoc rong") @Valid List<OrderItemRequest> items) {
}
