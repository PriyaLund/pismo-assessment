package com.example.pismo.controller;

import com.example.pismo.dto.AccountResponse;
import com.example.pismo.dto.TransactionRequest;
import com.example.pismo.dto.TransactionResponse;
import com.example.pismo.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {
    private final TransactionService txService;

    public TransactionController(TransactionService txService) {
        this.txService = txService;
    }

    @Operation(summary = "Create transaction")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse post(@RequestBody @Valid TransactionRequest req) {
        return txService.post(req);
    }

    @Operation(summary = "Get transaction by id")
    @GetMapping("/{id}")
    public TransactionResponse get(@PathVariable("id") Long id) {
        return txService.get(id);
    }

    @Operation(summary = "List transactions by account (paged)")
    @GetMapping
    public Page<TransactionResponse> listByAccount(
            @RequestParam(name = "account_id") Long accountId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return txService.findByAccount(accountId, pageable);
    }

}
