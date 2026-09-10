package com.example.orders.adapters.outbound.persistence.mapper;

import com.example.orders.adapters.outbound.persistence.entity.OrderItemJpaEntity;
import com.example.orders.adapters.outbound.persistence.entity.OrderJpaEntity;
import com.example.orders.domain.order.entity.CustomerId;
import com.example.orders.domain.order.entity.Money;
import com.example.orders.domain.order.entity.Order;
import com.example.orders.domain.order.entity.OrderId;
import com.example.orders.domain.order.entity.OrderItem;
import com.example.orders.domain.order.entity.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Dịch giữa entity domain và entity JPA.
 * Nhờ lớp này mà domain không phải mang annotation của JPA.
 */
@Component
public class OrderPersistenceMapper {

    public OrderJpaEntity toJpaEntity(Order order) {
        OrderJpaEntity jpa = new OrderJpaEntity(
                order.id().value(),
                order.customerId().value(),
                order.status().name(),
                order.totalAmount().amount(),
                order.placedAt(),
                order.rejectReason());

        for (OrderItem item : order.items()) {
            jpa.addItem(new OrderItemJpaEntity(
                    item.productId(), item.productName(), item.unitPrice().amount(), item.quantity()));
        }
        return jpa;
    }

    public Order toDomain(OrderJpaEntity jpa) {
        List<OrderItem> items = jpa.getItems().stream()
                .map(i -> new OrderItem(i.getProductId(), i.getProductName(),
                        Money.of(i.getUnitPrice()), i.getQuantity()))
                .toList();

        return Order.restore(
                OrderId.of(jpa.getId()),
                CustomerId.of(jpa.getCustomerId()),
                items,
                jpa.getPlacedAt(),
                OrderStatus.valueOf(jpa.getStatus()),
                Money.of(jpa.getTotalAmount()),
                jpa.getRejectReason());
    }
}
