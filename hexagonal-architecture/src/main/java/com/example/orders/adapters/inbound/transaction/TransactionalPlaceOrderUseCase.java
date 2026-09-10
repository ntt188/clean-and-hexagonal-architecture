package com.example.orders.adapters.inbound.transaction;

import com.example.orders.application.order.dto.PlaceOrderCommand;
import com.example.orders.application.order.dto.PlaceOrderResult;
import com.example.orders.application.order.port.in.PlaceOrderUseCase;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * DECORATOR GIAO DỊCH, đặt ở phía driving của hexagon.
 *
 * Trước đây mỗi lần gọi cổng ra là một giao dịch riêng, nên nếu bước sau hỏng thì
 * bước trước đã ghi vẫn nằm lại trong CSDL. Lớp này bọc TOÀN BỘ use case trong
 * một giao dịch duy nhất: hoặc mọi thay đổi cùng được ghi, hoặc không gì cả.
 *
 * Vì nó nằm ở tầng adapter nên {@code PlaceOrderService} vẫn không cần biết
 * Spring hay giao dịch là gì.
 */
public class TransactionalPlaceOrderUseCase implements PlaceOrderUseCase {

    private final PlaceOrderUseCase delegate;
    private final TransactionTemplate transactionTemplate;

    public TransactionalPlaceOrderUseCase(PlaceOrderUseCase delegate,
                                          TransactionTemplate transactionTemplate) {
        this.delegate = delegate;
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public PlaceOrderResult placeOrder(PlaceOrderCommand command) {
        // Đơn bị từ chối vì lý do nghiệp vụ KHÔNG ném ngoại lệ, nên giao dịch vẫn
        // được commit và bản ghi REJECTED được giữ lại để đối soát.
        return transactionTemplate.execute(status -> delegate.placeOrder(command));
    }
}
