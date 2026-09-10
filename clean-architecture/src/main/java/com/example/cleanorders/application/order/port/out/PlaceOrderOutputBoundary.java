package com.example.cleanorders.application.order.port.out;

import com.example.cleanorders.application.order.dto.PlaceOrderResponseModel;

/**
 * OUTPUT BOUNDARY — ĐIỂM KHÁC BIỆT LỚN NHẤT SO VỚI HEXAGONAL.
 * Use case không "return" kết quả mà gọi ngược ra interface này.
 * Luồng điều khiển đi ra ngoài, nhưng phụ thuộc source code vẫn hướng vào trong.
 */
public interface PlaceOrderOutputBoundary {
    void presentSuccess(PlaceOrderResponseModel responseModel);
    void presentFailure(String reason);
}
