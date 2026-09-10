package com.example.orders.adapters.outbound.notification;

import com.example.orders.application.order.port.out.NotificationPort;
import com.example.orders.domain.order.entity.Customer;
import com.example.orders.domain.order.entity.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * OUTBOUND ADAPTER cho cổng thông báo.
 *
 * Email là hiệu ứng KHÔNG THỂ THU HỒI. Nếu gửi ngay trong giao dịch mà giao dịch
 * sau đó rollback thì khách nhận được email về một đơn hàng không tồn tại.
 * Vì vậy adapter hoãn việc gửi tới mốc afterCommit. Việc này thuộc về adapter,
 * nên use case vẫn chỉ đơn giản gọi "hãy báo cho khách".
 */
@Component
public class EmailNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationAdapter.class);

    @Override
    public void notifyOrderConfirmed(Customer customer, Order order) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(customer, order);
                }
            });
        } else {
            // Không có giao dịch nào đang chạy (ví dụ khi unit test) thì gửi ngay.
            send(customer, order);
        }
    }

    private void send(Customer customer, Order order) {
        log.info("[EMAIL] Gui toi {}: don {} da xac nhan, tong {}",
                customer.email(), order.id(), order.totalAmount());
    }
}
