package com.example.pismo.controller;

import com.example.pismo.dto.AccountCreateRequest;
import com.example.pismo.entity.Account;
import com.example.pismo.repository.AccountRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiWebTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper om;
    @Autowired AccountRepository accountRepo;

    // Basic auth credentials (from SecurityConfig / application-test.yml)
    @Value("${app.security.user}")
     String USER;
    @Value("${app.security.password}")
     String PASSWORD;

    @Test
    @DisplayName("Given a valid document number, when creating an account, then response has id + document_number only")
    void givenValidDocument_whenCreateAccount_thenReturnsMinimalAccount() throws Exception {
        // Given
        var req = new AccountCreateRequest("12345678900", new BigDecimal("1000"));

        // When / Then
        mockMvc.perform(post("/accounts")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.account_id").exists())
                .andExpect(jsonPath("$.document_number", is("12345678900")))
                // ensure no accidental field appears
                .andExpect(jsonPath("$.available_balance").doesNotExist());
    }

    @Test
    @DisplayName("Given an existing account, when fetching by id, then response has id + document_number only")
    void givenExistingAccount_whenGetById_thenMinimalAccountResponse() throws Exception {
        // Given
        long accountId = createAccount("55544433322");

        // When / Then
        mockMvc.perform(get("/accounts/{id}", accountId).with(httpBasic(USER, PASSWORD)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.document_number", is("55544433322")))
                .andExpect(jsonPath("$.available_balance").doesNotExist());
    }

    @Test
    @DisplayName("Given account + transactions, when posting ops, then HTTP shows signed amounts and DB balance updates internally")
    void givenAccountAndTransactions_whenFlow_thenDbBalanceUpdates() throws Exception {
        // Given
        long accountId = createAccount("11122233344");

        // DB starts at 0.00 internally
        Account start = accountRepo.findById(accountId).orElseThrow();
        assertEquals(new BigDecimal("0.00"), start.getAvailableBalance());

        // When: PURCHASE 50.00 -> amount stored negative; balance -> -50.00 (DB-only)
        mockMvc.perform(post("/transactions")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"account_id\":%d,\"operation_type_id\":1,\"amount\":50.00}", accountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", closeTo(-50.0, 0.0001)))
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.operation_type_id").value(1));

        Account afterPurchase = accountRepo.findById(accountId).orElseThrow();
        assertEquals(new BigDecimal("-50.00"), afterPurchase.getAvailableBalance());

        // When: PAYMENT 60.00 -> amount positive; balance -> 10.00 (DB-only)
        mockMvc.perform(post("/transactions")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"account_id\":%d,\"operation_type_id\":4,\"amount\":60.00}", accountId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount", closeTo(60.0, 0.0001)))
                .andExpect(jsonPath("$.account_id").value(accountId))
                .andExpect(jsonPath("$.operation_type_id").value(4));

        Account afterPayment = accountRepo.findById(accountId).orElseThrow();
        assertEquals(new BigDecimal("10.00"), afterPayment.getAvailableBalance());
    }

    // ---------- helpers ----------

    private long createAccount(String documentNumber) throws Exception {
        var res = mockMvc.perform(post("/accounts")
                        .with(httpBasic(USER, PASSWORD))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(new AccountCreateRequest(documentNumber, new BigDecimal("1000")))))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = om.readTree(res.getResponse().getContentAsString());
        return node.get("account_id").asLong();
    }
}
