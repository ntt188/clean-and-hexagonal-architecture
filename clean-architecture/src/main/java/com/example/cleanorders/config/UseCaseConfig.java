package com.example.cleanorders.config;

import com.example.cleanorders.adapters.inbound.transaction.TransactionalPlaceOrderInputBoundary;
import com.example.cleanorders.adapters.inbound.web.presenter.PlaceOrderPresenter;
import com.example.cleanorders.application.order.port.in.PlaceOrderInputBoundary;
import com.example.cleanorders.application.order.port.out.CustomerGateway;
import com.example.cleanorders.application.order.port.out.InventoryGateway;
import com.example.cleanorders.application.order.port.out.NotificationGateway;
import com.example.cleanorders.application.order.port.out.OrderGateway;
import com.example.cleanorders.application.order.port.out.PlaceOrderOutputBoundary;
import com.example.cleanorders.application.order.usecase.PlaceOrderInteractor;
import com.example.cleanorders.domain.order.service.OrderPricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.annotation.RequestScope;

import java.time.Clock;

/**
 * COMPOSITION ROOT: lắp Interactor với các Gateway và Presenter.
 * Nhờ đó tầng domain và application không cần biết Spring tồn tại.
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

    /**
     * Presenter giữ trạng thái của MỘT lần gọi nên phải là request-scope.
     * Spring tạo proxy, vì vậy Interactor singleton vẫn tiêm được bean này.
     */
    @Bean
    @RequestScope
    public PlaceOrderPresenter placeOrderPresenter() {
        return new PlaceOrderPresenter();
    }

    /** Interactor thuần, không biết gì về giao dịch. */
    @Bean
    public PlaceOrderInteractor placeOrderInteractor(CustomerGateway customerGateway,
                                                     InventoryGateway inventoryGateway,
                                                     OrderGateway orderGateway,
                                                     NotificationGateway notificationGateway,
                                                     OrderPricingService orderPricingService,
                                                     PlaceOrderOutputBoundary outputBoundary,
                                                     Clock clock) {
        return new PlaceOrderInteractor(customerGateway, inventoryGateway, orderGateway,
                notificationGateway, orderPricingService, outputBoundary, clock);
    }

    /** Bản ĐÃ BỌC GIAO DỊCH — đây là bean mà Controller nhận được. */
    @Bean
    @Primary
    public PlaceOrderInputBoundary placeOrderInputBoundary(PlaceOrderInteractor delegate,
                                                           TransactionTemplate transactionTemplate) {
        return new TransactionalPlaceOrderInputBoundary(delegate, transactionTemplate);
    }
}
