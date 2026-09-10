package com.example.orders.domain.order.entity;

import com.example.orders.domain.order.exception.DomainException;

/**
 * Entity san pham trong kho.
 *
 * Truoc day du lieu san pham la mot record long trong cong ra
 * ({@code InventoryPort.ProductInfo}), khien use case phai tu tay kiem tra ton kho
 * roi tu tay dung {@code new OrderItem(...)} tung truong mot — tuc la lam ca viec
 * mapping. Dua no vao domain thi hai quy tac do ve dung cho cua chung.
 */
public record Product(String id, String name, Money unitPrice, int availableQuantity) {

    public Product {
        if (id == null || id.isBlank())
            throw new DomainException("Ma san pham khong duoc rong");
        if (availableQuantity < 0)
            throw new DomainException("Ton kho khong duoc am (san pham: " + id + ")");
    }

    /** Quy tac nghiep vu: kho co du hang cho so luong nay khong. */
    public boolean hasStockFor(int quantity) {
        return availableQuantity >= quantity;
    }

    /** Domain tu dung dong hang cua chinh no, use case khong phai map tung truong. */
    public OrderItem orderLine(int quantity) {
        return new OrderItem(id, name, unitPrice, quantity);
    }
}
