package com.example.orders.adapters.inbound.web.response;

/** DTO trả về cho client. Hình dạng JSON là chuyện của web, không phải của lõi. */
public record PlaceOrderResponse(String orderId, String status, String customerName,
                                 String totalAmount, String reason) {
}
