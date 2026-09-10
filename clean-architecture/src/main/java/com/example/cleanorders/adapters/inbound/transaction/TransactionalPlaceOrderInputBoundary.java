package com.example.cleanorders.adapters.inbound.transaction;

import com.example.cleanorders.application.order.dto.PlaceOrderRequestModel;
import com.example.cleanorders.application.order.port.in.PlaceOrderInputBoundary;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * DECORATOR GIAO DỊCH bọc Input Boundary.
 *
 * Nằm ở vòng Interface Adapters nên {@code PlaceOrderInteractor} vẫn là POJO thuần.
 * Toàn bộ use case chạy trong một giao dịch duy nhất.
 */
public class TransactionalPlaceOrderInputBoundary implements PlaceOrderInputBoundary {

    private final PlaceOrderInputBoundary delegate;
    private final TransactionTemplate transactionTemplate;

    public TransactionalPlaceOrderInputBoundary(PlaceOrderInputBoundary delegate,
                                                TransactionTemplate transactionTemplate) {
        this.delegate = delegate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public void execute(PlaceOrderRequestModel requestModel) {
        transactionTemplate.executeWithoutResult(status -> delegate.execute(requestModel));
    }
}
