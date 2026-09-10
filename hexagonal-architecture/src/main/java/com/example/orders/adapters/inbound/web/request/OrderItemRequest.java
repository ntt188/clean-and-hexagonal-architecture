package com.example.orders.adapters.inbound.web.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** DTO của tầng web. Chỉ dùng cho JSON, không lọt vào lõi. */
public record OrderItemRequest(
        @NotBlank(message = "productId khong duoc rong") String productId,
        @Min(value = 1, message = "quantity phai lon hon 0") int quantity) {
}
