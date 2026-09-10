package com.example.orders.domain.order.entity;

import com.example.orders.domain.order.exception.DomainException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Value Object số tiền: luôn 2 chữ số thập phân, không bao giờ âm. */
public record Money(BigDecimal amount) {

    public Money {
        if (amount == null) throw new DomainException("So tien khong duoc null");
        if (amount.signum() < 0) throw new DomainException("So tien khong duoc am");
        amount = amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static Money of(String value)     { return new Money(new BigDecimal(value)); }
    public static Money of(BigDecimal value) { return new Money(value); }
    public static Money zero()               { return new Money(BigDecimal.ZERO); }

    public Money plus(Money other)   { return new Money(this.amount.add(other.amount)); }
    public Money minus(Money other)  { return new Money(this.amount.subtract(other.amount)); }
    public Money times(int quantity) { return new Money(this.amount.multiply(BigDecimal.valueOf(quantity))); }

    public Money percentage(BigDecimal rate) { return new Money(this.amount.multiply(rate)); }

    public boolean isGreaterThan(Money other) { return this.amount.compareTo(other.amount) > 0; }

    @Override public String toString() { return amount.toPlainString(); }
}
