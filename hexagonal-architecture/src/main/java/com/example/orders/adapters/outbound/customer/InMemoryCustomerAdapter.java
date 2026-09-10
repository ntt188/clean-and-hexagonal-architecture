package com.example.orders.adapters.outbound.customer;

import com.example.orders.application.order.port.out.CustomerRepository;
import com.example.orders.domain.order.entity.Customer;
import com.example.orders.domain.order.entity.CustomerId;
import com.example.orders.domain.order.entity.Money;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * OUTBOUND ADAPTER: giả lập hệ thống khách hàng bên ngoài.
 * Đổi sang gọi REST sang Customer Service chỉ cần viết lớp khác cài cùng cổng ra.
 */
@Component
public class InMemoryCustomerAdapter implements CustomerRepository {

    private final Map<String, Customer> table = new LinkedHashMap<>();

    public InMemoryCustomerAdapter() {
        table.put("C-01", new Customer(CustomerId.of("C-01"), "Nguyen Van A",
                "a@example.com", Money.of("1000.00")));
        table.put("C-02", new Customer(CustomerId.of("C-02"), "Tran Thi B",
                "b@example.com", Money.of("50.00")));
    }

    @Override
    public Optional<Customer> findById(CustomerId customerId) {
        return Optional.ofNullable(table.get(customerId.value()));
    }
}
