package com.example.orders.application.order.dto;

import com.example.orders.domain.order.exception.InvalidOrderException;

import java.util.List;

/**
 * Command đi vào qua cổng vào. Là DTO của tầng application (không phải entity,
 * cũng không phải request của web) và tự kiểm tra tính hợp lệ ngay tại biên.
 */
public record PlaceOrderCommand(String customerId, List<Item> items) {

    public PlaceOrderCommand {
        if (customerId == null || customerId.isBlank())
            throw new InvalidOrderException("Thieu ma khach hang");
        if (items == null || items.isEmpty())
            throw new InvalidOrderException("Gio hang trong");
        items = List.copyOf(items);
    }

    public record Item(String productId, int quantity) {}
}
