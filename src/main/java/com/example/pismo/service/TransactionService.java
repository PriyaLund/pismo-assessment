package com.example.pismo.service;

import com.example.pismo.dto.TransactionRequest;
import com.example.pismo.dto.TransactionResponse;
import com.example.pismo.entity.Account;
import com.example.pismo.entity.Transaction;
import com.example.pismo.exception.BusinessException;
import com.example.pismo.repository.AccountRepository;
import com.example.pismo.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final AccountRepository accountRepo;
    private final TransactionRepository txRepo;

    public TransactionService(AccountRepository accountRepo, TransactionRepository txRepo) {
        this.accountRepo = accountRepo;
        this.txRepo = txRepo;
    }

    @Transactional
    public TransactionResponse post(TransactionRequest req) {
        Account account = accountRepo.findById(req.accountId())
                .orElseThrow(() -> new BusinessException("ACCOUNT_NOT_FOUND", "Account not found"));

        int opId = validateOperationType(req.operationTypeId());
        BigDecimal signedAmount = normalizeAmountSign(opId, req.amount());

        // Persist transaction with signed amount
        Transaction tx = new Transaction(account, opId, signedAmount);
        txRepo.save(tx);

        // Update account balance: add signed amount
        account.addToBalance(signedAmount);
        accountRepo.save(account);

        return new TransactionResponse(
                tx.getId(),
                account.getId(),
                tx.getOperationTypeId(),
                tx.getAmount()
        );
    }

    private int validateOperationType(Integer id) {
        if (id == null || id < 1 || id > 4) throw new BusinessException("INVALID_OPERATION_TYPE", "Valid ids: 1..4");
        return id;
    }

    private BigDecimal normalizeAmountSign(int opId, BigDecimal amount) {
        if (amount == null) throw new BusinessException("INVALID_AMOUNT", "Amount is required");
        BigDecimal abs = amount.abs();
        return (opId == 4) ? abs : abs.negate();
    }
}
