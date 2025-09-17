package com.example.pismo.service;

import com.example.pismo.dto.AccountCreateRequest;
import com.example.pismo.dto.AccountResponse;
import com.example.pismo.entity.Account;
import com.example.pismo.exception.BusinessException;
import com.example.pismo.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository accountRepo;

    public AccountService(AccountRepository accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Transactional
    public AccountResponse create(AccountCreateRequest req) {
        accountRepo.findByDocumentNumber(req.documentNumber())
                .ifPresent(a -> {
                    throw new BusinessException("ACCOUNT_EXISTS",
                            "Account already exists for document " + req.documentNumber());
                });

        Account a = new Account(req.documentNumber(), req.creditLimit());
        accountRepo.save(a);
        return toResponse(a);
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long id) {
        Account a = accountRepo.findById(id)
                .orElseThrow(() -> new BusinessException("ACCOUNT_NOT_FOUND", "Account not found " + id));
        return toResponse(a);
    }

    private AccountResponse toResponse(Account a) {
        return new AccountResponse(a.getId(), a.getDocumentNumber(), a.getCreditLimit());
    }
}
