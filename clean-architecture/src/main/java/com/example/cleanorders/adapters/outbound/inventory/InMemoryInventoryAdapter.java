package com.example.cleanorders.adapters.outbound.inventory;

import com.example.cleanorders.application.order.port.out.InventoryGateway;
import com.example.cleanorders.domain.order.entity.Money;
import com.example.cleanorders.domain.order.entity.Product;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** OUTBOUND ADAPTER: gia lap he thong kho. */
@Component
public class InMemoryInventoryAdapter implements InventoryGateway {

    private final Map<String, Product> catalog = new LinkedHashMap<>();

    public InMemoryInventoryAdapter() {
        catalog.put("P-01", new Product("P-01", "Ban phim co", Money.of("120.00"), 10));
        catalog.put("P-02", new Product("P-02", "Chuot khong day", Money.of("45.50"), 3));
        catalog.put("P-03", new Product("P-03", "Man hinh 27 inch", Money.of("320.00"), 0));
    }

    @Override
    public Optional<Product> findProduct(String productId) {
        return Optional.ofNullable(catalog.get(productId));
    }

    @Override
    public void reserve(String productId, int quantity) {
        Product p = catalog.get(productId);
        catalog.put(productId, new Product(p.id(), p.name(), p.unitPrice(), p.availableQuantity() - quantity));
    }
}
