package com.example.cleanorders.application.order.port.in;

import com.example.cleanorders.application.order.dto.PlaceOrderRequestModel;

/**
 * INPUT BOUNDARY: cổng vào của use case.
 * Trả về void — kết quả được ĐẨY ra qua Output Boundary.
 */
public interface PlaceOrderInputBoundary {
    void execute(PlaceOrderRequestModel requestModel);
}
