package com.example.cleanorders.adapters.inbound.web.presenter;

/**
 * VIEW MODEL: dữ liệu ĐÃ định dạng sẵn cho tầng hiển thị.
 * View (ở đây là Controller) chỉ việc lấy ra dùng, không phải xử lý gì thêm.
 */
public record OrderViewModel(boolean success, int httpStatus, String orderId, String status,
                             String customerName, String totalAmount, String message) {
}
