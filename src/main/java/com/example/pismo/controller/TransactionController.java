package com.example.pismo.controller;

import com.example.pismo.dto.TransactionRequest;
import com.example.pismo.dto.TransactionResponse;
import com.example.pismo.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
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
}
