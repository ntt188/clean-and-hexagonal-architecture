package com.example.cleanorders.adapters.outbound.persistence;

import com.example.cleanorders.adapters.outbound.persistence.entity.OrderJpaEntity;
import com.example.cleanorders.adapters.outbound.persistence.mapper.OrderPersistenceMapper;
import com.example.cleanorders.adapters.outbound.persistence.springdata.SpringDataOrderJpaRepository;
import com.example.cleanorders.application.order.port.out.OrderGateway;
import com.example.cleanorders.domain.order.entity.Order;
import com.example.cleanorders.domain.order.entity.OrderId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** OUTBOUND ADAPTER: cắm Spring Data JPA vào cổng ra OrderGateway. */
@Component
public class JpaOrderGatewayAdapter implements OrderGateway {

    private final SpringDataOrderJpaRepository jpaRepository;
    private final OrderPersistenceMapper mapper;

    public JpaOrderGatewayAdapter(SpringDataOrderJpaRepository jpaRepository,
                                     OrderPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    /**
     * MANDATORY: bat buoc da co giao dich do decorator mo san.
     * Neu ai do goi cong ra ngoai giao dich, Spring se nem loi ngay thay vi
     * am tham tao mot giao dich rieng — chinh la loi cu.
     */
    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Order save(Order order) {
        OrderJpaEntity saved = jpaRepository.save(mapper.toJpaEntity(order));
        return mapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(OrderId orderId) {
        return jpaRepository.findById(orderId.value()).map(mapper::toDomain);
    }
}
