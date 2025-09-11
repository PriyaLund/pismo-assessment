package com.example.pismo.service;

import com.example.pismo.dto.TransactionRequest;
import com.example.pismo.entity.Account;
import com.example.pismo.exception.BusinessException;
import com.example.pismo.repository.AccountRepository;
import com.example.pismo.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(TransactionService.class)
class TransactionServiceTest {

    @Autowired AccountRepository accountRepo;
    @Autowired TransactionRepository txRepo;
    @Autowired TransactionService txService;

    Account account;

    @BeforeEach
    void setup() {
        account = accountRepo.save(new Account("12345678900"));
        assertEquals(new BigDecimal("0.00"), account.getAvailableBalance());
    }

    @Test
    void givenPurchase_whenPost_thenAmountNegative_andBalanceDecreases() {
        // When
        var res = txService.post(new TransactionRequest(account.getId(), 1, new BigDecimal("50.00")));

        // Then: amount stored negative
        assertEquals(new BigDecimal("-50.00"), res.amount());

        // And: balance decreased to -50.00
        Account reloaded = accountRepo.findById(account.getId()).orElseThrow();
        assertEquals(0, reloaded.getAvailableBalance().compareTo(new BigDecimal("-50.00")));

    }

    @Test
    void givenPayment_whenPost_thenAmountPositive_andBalanceIncreases() {
        // Given: start with -20.00 (purchase)
        txService.post(new TransactionRequest(account.getId(), 1, new BigDecimal("20.00")));

        // When: payment of 30.00
        var res = txService.post(new TransactionRequest(account.getId(), 4, new BigDecimal("30.00")));

        // Then: amount positive
        assertEquals(new BigDecimal("30.00"), res.amount());

        // And: balance -20 + 30 = 10.00
        Account reloaded = accountRepo.findById(account.getId()).orElseThrow();
        assertEquals(0, reloaded.getAvailableBalance().compareTo(new BigDecimal("10.00")));
    }

    @Test
    void givenWithdrawal_whenPost_thenAmountNegative_andBalanceDecreases() {
        // When
        txService.post(new TransactionRequest(account.getId(), 3, new BigDecimal("18.70")));

        // Then: balance -18.70
        Account reloaded = accountRepo.findById(account.getId()).orElseThrow();
        assertEquals(0, reloaded.getAvailableBalance().compareTo(new BigDecimal("-18.70")));
    }

}
