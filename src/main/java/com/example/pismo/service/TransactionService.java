package com.example.pismo.service;

import com.example.pismo.dto.TransactionRequest;
import com.example.pismo.dto.TransactionResponse;
import com.example.pismo.entity.Account;
import com.example.pismo.entity.Transaction;
import com.example.pismo.exception.BusinessException;
import com.example.pismo.repository.AccountRepository;
import com.example.pismo.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        if (req.amount() == null || req.amount().signum() <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Amount must be > 0");
        }

        // normalize: opId 4 (payment) => +amount, others => -amount
        BigDecimal signedAmount = normalizeAmountSign(opId, req.amount());

        // --- validate against limit BEFORE mutating state ---
        if (opId != 4) { // debit operations only
            BigDecimal available = account.getAvailableBalance().add(account.getCreditLimit());
            // compare using the positive magnitude of the debit
            BigDecimal debit = req.amount().abs();
            if (debit.compareTo(available) > 0) {
                throw new BusinessException(
                        "TOTAL_LIMIT_EXCEEDED",
                        "Transaction amount exceeds available limit"
                );
            }
        }

        // persist and apply
        Transaction tx = new Transaction(account, opId, signedAmount);
        txRepo.save(tx);

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

    @Transactional(readOnly = true)
    public TransactionResponse get(Long id) {
        Transaction tx = txRepo.findById(id)
                .orElseThrow(() -> new BusinessException("TRANSACTION_NOT_FOUND", "Transaction not found: " + id));
        return toResponse(tx);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> findByAccount(Long accountId, Pageable pageable) {
        return txRepo.findByAccountId(accountId, pageable)
                .map(this::toResponse);
    }

    private TransactionResponse toResponse(Transaction tx) {
        return new TransactionResponse(tx.getId(), tx.getAccount().getId(), tx.getOperationTypeId(), tx.getAmount());
    }
}
