package com.example.orders.application.order.dto;

/**
 * Kết quả TRẢ VỀ qua cổng vào.
 * Hexagonal: use case trả thẳng kết quả, adapter tự quyết định cách trình bày.
 */
public record PlaceOrderResult(boolean accepted, String orderId, String status,
                               String customerName, String totalAmount, String reason) {

    public static PlaceOrderResult accepted(String orderId, String status,
                                            String customerName, String totalAmount) {
        return new PlaceOrderResult(true, orderId, status, customerName, totalAmount, null);
    }

    public static PlaceOrderResult rejected(String reason) {
        return new PlaceOrderResult(false, null, "REJECTED", null, null, reason);
    }
}
