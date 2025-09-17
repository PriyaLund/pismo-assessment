package com.example.pismo.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transactions", indexes = {@Index(name = "idx_tx_account", columnList = "account_id")})
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    @Column(name = "operation_type_id", nullable = false)
    private Integer operationTypeId;
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(name = "event_date", nullable = false)
    private OffsetDateTime eventDate = OffsetDateTime.now();

    protected Transaction() {
    }

    public Transaction(Account account, Integer operationTypeId, BigDecimal amount) {
        this.account = account;
        this.operationTypeId = operationTypeId;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public Integer getOperationTypeId() {
        return operationTypeId;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
