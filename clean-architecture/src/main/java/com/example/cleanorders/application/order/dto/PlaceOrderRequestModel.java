package com.example.cleanorders.application.order.dto;

import com.example.cleanorders.domain.order.exception.InvalidOrderException;

import java.util.List;

/**
 * REQUEST MODEL (Clean Architecture): cấu trúc dữ liệu thuần đi VÀO use case
 * qua Input Boundary. Không phải entity, cũng không phải request của web.
 */
public record PlaceOrderRequestModel(String customerId, List<Item> items) {

    public PlaceOrderRequestModel {
        if (customerId == null || customerId.isBlank())
            throw new InvalidOrderException("Thieu ma khach hang");
        if (items == null || items.isEmpty())
            throw new InvalidOrderException("Gio hang trong");
        items = List.copyOf(items);
    }

    public record Item(String productId, int quantity) {}
}
