package com.example.orders.adapters.outbound.persistence.springdata;

import com.example.orders.adapters.outbound.persistence.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository của Spring Data — chi tiết kỹ thuật, nằm hẳn ở tầng adapter. */
public interface SpringDataOrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {
}
