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
        account = accountRepo.save(new Account("12345678900", new BigDecimal("1000")));
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

    @Test
    void givenDebitWithinCreditLimit_whenPost_thenSucceeds_andBalanceIsNegative() {
        // Given: creditLimit = 1000, balance = 0.00 (from setup)
        // When: debit/purchase of 700.00
        var res = txService.post(new TransactionRequest(account.getId(), 1, new BigDecimal("700.00")));

        // Then: amount stored negative
        assertEquals(new BigDecimal("-700.00"), res.amount());

        // And: balance is -700.00
        Account reloaded = accountRepo.findById(account.getId()).orElseThrow();
        assertEquals(0, reloaded.getAvailableBalance().compareTo(new BigDecimal("-700.00")));
    }

    @Test
    void givenDebitExceedsTotalLimit_whenPost_thenThrows_andNoMutation() {
        // Given: total limit = availableBalance (0.00) + creditLimit (1000.00) = 1000.00
        long beforeCount = txRepo.count();

        // When + Then: debit > total limit should throw
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> txService.post(new TransactionRequest(account.getId(), 1, new BigDecimal("1200.00")))
        );
        // Optional: confirm error code/message if exposed via getters
        // assertEquals("TOTAL_LIMIT_EXCEEDED", ex.getCode());

        // And: balance unchanged
        Account reloaded = accountRepo.findById(account.getId()).orElseThrow();
        assertEquals(0, reloaded.getAvailableBalance().compareTo(new BigDecimal("0.00")));

        // And: no transaction persisted
        assertEquals(beforeCount, txRepo.count());
    }

    @Test
    void givenRemainingLimitExactlyEnough_whenPost_thenBoundaryPasses() {
        // Given: first debit uses 990, leaving remaining limit 10
        txService.post(new TransactionRequest(account.getId(), 1, new BigDecimal("990.00")));
        Account afterFirst = accountRepo.findById(account.getId()).orElseThrow();
        assertEquals(0, afterFirst.getAvailableBalance().compareTo(new BigDecimal("-990.00")));

        // When: second debit exactly equals remaining limit (10.00)
        var res = txService.post(new TransactionRequest(account.getId(), 1, new BigDecimal("10.00")));

        // Then: stored as -10.00
        assertEquals(new BigDecimal("-10.00"), res.amount());

        // And: balance becomes -1000.00 (exactly at limit)
        Account reloaded = accountRepo.findById(account.getId()).orElseThrow();
        assertEquals(0, reloaded.getAvailableBalance().compareTo(new BigDecimal("-1000.00")));
    }


}
