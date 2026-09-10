package com.example.cleanorders.application.order.dto;

/**
 * RESPONSE MODEL: dữ liệu thô đi RA khỏi use case qua Output Boundary.
 * Chưa được định dạng để hiển thị — việc đó là của Presenter.
 */
public record PlaceOrderResponseModel(String orderId, String status, String customerName,
                                      String totalAmount) {
}
