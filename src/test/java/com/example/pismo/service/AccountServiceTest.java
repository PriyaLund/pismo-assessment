package com.example.pismo.service;

import com.example.pismo.dto.AccountCreateRequest;
import com.example.pismo.dto.AccountResponse;
import com.example.pismo.entity.Account;
import com.example.pismo.exception.BusinessException;
import com.example.pismo.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(AccountService.class)
class AccountServiceTest {

    @Autowired AccountRepository accountRepo;
    @Autowired AccountService accountService;

    @BeforeEach
    void cleanDatabase() {
        accountRepo.deleteAll();
    }

    @Test
    void givenNewAccount_whenCreate_thenAvailableBalanceIsZero() {
        // Given
        String doc = "12345678900";

        // When
        AccountResponse res = accountService.create(new AccountCreateRequest(doc));

        // Then
        assertNotNull(res.accountId());
        assertEquals(doc, res.documentNumber());

        // Check balance internally from DB entity
        Account persisted = accountRepo.findById(res.accountId()).orElseThrow();
        assertEquals(0, persisted.getAvailableBalance().compareTo(BigDecimal.ZERO));
    }



    @Test
    void givenNewDocumentNumber_whenCreateAccount_thenAccountIsPersisted() {
        // Given
        String documentNumber = "12345678900";

        // When
        AccountResponse response = accountService.create(new AccountCreateRequest(documentNumber));

        // Then
        assertNotNull(response.accountId());
        assertEquals(documentNumber, response.documentNumber());

        Account persisted = accountRepo.findById(response.accountId()).orElseThrow();
        assertEquals(documentNumber, persisted.getDocumentNumber());
    }

    @Test
    void givenExistingDocumentNumber_whenCreateAccount_thenThrowsBusinessException() {
        // Given
        String documentNumber = "99999999999";
        accountService.create(new AccountCreateRequest(documentNumber));

        // When + Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> accountService.create(new AccountCreateRequest(documentNumber)));

        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void givenExistingAccountId_whenGetAccount_thenReturnsAccountResponse() {
        // Given
        Account saved = accountRepo.save(new Account("55544433322"));

        // When
        AccountResponse response = accountService.get(saved.getId());

        // Then
        assertEquals(saved.getId(), response.accountId());
        assertEquals("55544433322", response.documentNumber());
    }

    @Test
    void givenNonExistingAccountId_whenGetAccount_thenThrowsBusinessException() {
        // Given
        long invalidId = 9999L;

        // When + Then
        BusinessException ex = assertThrows(BusinessException.class,
                () -> accountService.get(invalidId));

        assertTrue(ex.getMessage().contains("not found"));
    }
}
