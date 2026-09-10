package com.example.orders.adapters.inbound.web.mapper;

import com.example.orders.adapters.inbound.web.request.OrderItemRequest;
import com.example.orders.adapters.inbound.web.request.PlaceOrderRequest;
import com.example.orders.adapters.inbound.web.response.PlaceOrderResponse;
import com.example.orders.application.order.dto.PlaceOrderCommand;
import com.example.orders.application.order.dto.PlaceOrderResult;
import org.springframework.stereotype.Component;

import java.util.List;

/** Chuyển đổi giữa DTO của web và DTO của tầng application. */
@Component
public class OrderWebMapper {

    public PlaceOrderCommand toCommand(PlaceOrderRequest request) {
        List<PlaceOrderCommand.Item> items = request.items().stream()
                .map(this::toCommandItem)
                .toList();
        return new PlaceOrderCommand(request.customerId(), items);
    }

    private PlaceOrderCommand.Item toCommandItem(OrderItemRequest item) {
        return new PlaceOrderCommand.Item(item.productId(), item.quantity());
    }

    public PlaceOrderResponse toResponse(PlaceOrderResult result) {
        return new PlaceOrderResponse(result.orderId(), result.status(),
                result.customerName(), result.totalAmount(), result.reason());
    }
}
