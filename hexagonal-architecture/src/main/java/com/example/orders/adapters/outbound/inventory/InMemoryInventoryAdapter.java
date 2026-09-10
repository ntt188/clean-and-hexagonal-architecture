package com.example.orders.adapters.outbound.inventory;

import com.example.orders.application.order.port.out.InventoryPort;
import com.example.orders.domain.order.entity.Money;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** OUTBOUND ADAPTER: giả lập hệ thống kho. */
@Component
public class InMemoryInventoryAdapter implements InventoryPort {

    private final Map<String, ProductInfo> catalog = new LinkedHashMap<>();

    public InMemoryInventoryAdapter() {
        catalog.put("P-01", new ProductInfo("P-01", "Ban phim co", Money.of("120.00"), 10));
        catalog.put("P-02", new ProductInfo("P-02", "Chuot khong day", Money.of("45.50"), 3));
        catalog.put("P-03", new ProductInfo("P-03", "Man hinh 27 inch", Money.of("320.00"), 0));
    }

    @Override
    public Optional<ProductInfo> findProduct(String productId) {
        return Optional.ofNullable(catalog.get(productId));
    }

    @Override
    public void reserve(String productId, int quantity) {
        ProductInfo p = catalog.get(productId);
        catalog.put(productId, new ProductInfo(p.productId(), p.name(), p.unitPrice(), p.available() - quantity));
    }
}
