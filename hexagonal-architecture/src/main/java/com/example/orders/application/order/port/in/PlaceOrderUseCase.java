package com.example.orders.application.order.port.in;

import com.example.orders.application.order.dto.PlaceOrderCommand;
import com.example.orders.application.order.dto.PlaceOrderResult;

/** INBOUND PORT (driving): hợp đồng để bên ngoài điều khiển lõi nghiệp vụ. */
public interface PlaceOrderUseCase {
    PlaceOrderResult placeOrder(PlaceOrderCommand command);
}
