package com.example.cleanorders.adapters.inbound.web.mapper;

import com.example.cleanorders.adapters.inbound.web.presenter.OrderViewModel;
import com.example.cleanorders.adapters.inbound.web.request.OrderItemRequest;
import com.example.cleanorders.adapters.inbound.web.request.PlaceOrderRequest;
import com.example.cleanorders.adapters.inbound.web.response.PlaceOrderResponse;
import com.example.cleanorders.application.order.dto.PlaceOrderRequestModel;
import org.springframework.stereotype.Component;

import java.util.List;

/** Chuyển đổi giữa DTO của web, Request Model của use case và View Model. */
@Component
public class OrderWebMapper {

    public PlaceOrderRequestModel toRequestModel(PlaceOrderRequest request) {
        List<PlaceOrderRequestModel.Item> items = request.items().stream()
                .map(this::toRequestModelItem)
                .toList();
        return new PlaceOrderRequestModel(request.customerId(), items);
    }

    private PlaceOrderRequestModel.Item toRequestModelItem(OrderItemRequest item) {
        return new PlaceOrderRequestModel.Item(item.productId(), item.quantity());
    }

    public PlaceOrderResponse toResponse(OrderViewModel viewModel) {
        return new PlaceOrderResponse(viewModel.orderId(), viewModel.status(),
                viewModel.customerName(), viewModel.totalAmount(), viewModel.message());
    }
}
