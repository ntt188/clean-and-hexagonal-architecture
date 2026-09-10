package com.example.cleanorders.adapters.outbound.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @Column(name = "id", length = 36)
    private String id;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "placed_at", nullable = false)
    private Instant placedAt;

    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    protected OrderJpaEntity() {
    }

    public OrderJpaEntity(String id, String customerId, String status, BigDecimal totalAmount,
                          Instant placedAt, String rejectReason) {
        this.id = id;
        this.customerId = customerId;
        this.status = status;
        this.totalAmount = totalAmount;
        this.placedAt = placedAt;
        this.rejectReason = rejectReason;
    }

    public void addItem(OrderItemJpaEntity item) {
        item.assignTo(this);
        this.items.add(item);
    }

    public String getId()                    { return id; }
    public String getCustomerId()            { return customerId; }
    public String getStatus()                { return status; }
    public BigDecimal getTotalAmount()       { return totalAmount; }
    public Instant getPlacedAt()             { return placedAt; }
    public String getRejectReason()          { return rejectReason; }
    public List<OrderItemJpaEntity> getItems() { return items; }
}
