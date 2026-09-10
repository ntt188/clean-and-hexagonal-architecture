package com.example.orders.config;

import com.example.orders.adapters.inbound.transaction.TransactionalPlaceOrderUseCase;
import com.example.orders.application.order.port.in.PlaceOrderUseCase;
import com.example.orders.application.order.port.out.CustomerRepository;
import com.example.orders.application.order.port.out.InventoryPort;
import com.example.orders.application.order.port.out.NotificationPort;
import com.example.orders.application.order.port.out.OrderRepository;
import com.example.orders.application.order.usecase.PlaceOrderService;
import com.example.orders.domain.order.service.OrderPricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

/**
 * COMPOSITION ROOT: nơi CẮM adapter vào port.
 * Vì việc lắp ráp nằm ở đây nên domain và application không cần biết Spring tồn tại.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public OrderPricingService orderPricingService() {
        return new OrderPricingService();
    }

    /** Use case thuần, không biết gì về giao dịch. */
    @Bean
    public PlaceOrderService placeOrderService(CustomerRepository customerRepository,
                                               InventoryPort inventoryPort,
                                               OrderRepository orderRepository,
                                               NotificationPort notificationPort,
                                               OrderPricingService orderPricingService,
                                               Clock clock) {
        return new PlaceOrderService(customerRepository, inventoryPort, orderRepository,
                notificationPort, orderPricingService, clock);
    }

    /**
     * Bean được tiêm vào Controller là bản ĐÃ BỌC GIAO DỊCH.
     * {@code @Primary} để Spring chọn nó thay vì bean {@code placeOrderService} bên trên.
     */
    @Bean
    @Primary
    public PlaceOrderUseCase placeOrderUseCase(PlaceOrderService delegate,
                                               TransactionTemplate transactionTemplate) {
        return new TransactionalPlaceOrderUseCase(delegate, transactionTemplate);
    }
}
