package com.example.pismo.controller;

import com.example.pismo.dto.AccountCreateRequest;
import com.example.pismo.dto.AccountResponse;
import com.example.pismo.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Operation(summary = "Create account")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@RequestBody @Valid AccountCreateRequest req) {
        return accountService.create(req);
    }

    @Operation(summary = "Get account by id")
    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable("id") Long id) {
        return accountService.get(id);
    }

}
