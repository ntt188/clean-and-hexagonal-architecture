package com.example.orders.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Giới hạn JPA chỉ quét trong tầng adapter.
 * Nhờ vậy không có nguy cơ vô tình biến entity domain thành entity JPA.
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = "com.example.orders.adapters.outbound.persistence.springdata")
@EntityScan(basePackages = "com.example.orders.adapters.outbound.persistence.entity")
public class PersistenceConfig {
}
