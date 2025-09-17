package com.example.pismo.entity;// imports
import java.math.BigDecimal;
import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long id;

    @Column(name = "document_number", nullable = false, unique = true)
    private String documentNumber;

    @Column(name = "available_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal availableBalance = BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);

    @Column(name = "credit_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal creditLimit;

    protected Account() {}

    public Account(String documentNumber, BigDecimal creditLimit) {
        this.documentNumber = documentNumber;
        this.availableBalance = BigDecimal.ZERO.setScale(2, BigDecimal.ROUND_HALF_UP);
        this.creditLimit = creditLimit.setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public Long getId() { return id; }
    public String getDocumentNumber() { return documentNumber; }
    public BigDecimal getAvailableBalance() { return availableBalance; }

    public void addToBalance(BigDecimal delta) {
        this.availableBalance = this.availableBalance
                .add(delta)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    @PrePersist
    @PreUpdate
    private void normalizeScale() {
        if (availableBalance != null) {
            availableBalance = availableBalance.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
    }
}
