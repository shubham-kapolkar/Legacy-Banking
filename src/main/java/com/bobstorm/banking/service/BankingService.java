package com.bobstorm.banking.service;

import com.bobstorm.banking.model.Account;
import com.bobstorm.banking.model.Customer;
import com.bobstorm.banking.model.Transaction;
import com.bobstorm.banking.repository.InMemoryBankRepository;

import java.util.List;

/**
 * Top-level facade used by Main.java.
 * Coordinates AccountService, TransactionService, and ReportService.
 *
 * =============================================================
 * INTENTIONAL LEGACY CHARACTERISTICS — do not "fix" before demo
 * =============================================================
 * 1. Inconsistent delegation: createCustomer/createAccount delegate to
 *    AccountService; deposit/withdraw/transfer delegate to TransactionService.
 *    However, BankingService also re-validates amounts before forwarding,
 *    producing duplicated validation across the call stack.
 * 2. BankingService constructs its own service instances internally rather
 *    than accepting them via constructor injection — tight coupling.
 * 3. The facade leaks internal model objects to callers directly.
 * =============================================================
 */
public class BankingService {

    private final InMemoryBankRepository repository;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final ReportService reportService;

    // Legacy: constructs concrete collaborators internally (no dependency injection framework)
    public BankingService() {
        this.repository = new InMemoryBankRepository();
        this.accountService = new AccountService(repository);
        this.transactionService = new TransactionService(repository);
        this.reportService = new ReportService(repository);
    }

    // ---- Customer / Account management ----

    public Customer createCustomer(String customerId, String name, String email, String phone) {
        return accountService.createCustomer(customerId, name, email, phone);
    }

    public Account createAccount(String accountId, String customerId, double initialBalance) {
        return accountService.createAccount(accountId, customerId, initialBalance);
    }

    public Account getAccount(String accountId) {
        return accountService.getAccount(accountId);
    }

    public Customer getCustomer(String customerId) {
        return accountService.getCustomer(customerId);
    }

    public void deactivateAccount(String accountId) {
        accountService.deactivateAccount(accountId);
    }

    // ---- Transaction operations ----

    /**
     * Deposit with duplicated pre-validation before delegating to TransactionService.
     * Intentional legacy smell: validation is repeated at every layer.
     */
    public Transaction deposit(String accountId, double amount) {
        // Duplicated BR-001 guard — TransactionService will check again
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return transactionService.deposit(accountId, amount);
    }

    /**
     * Withdraw with duplicated pre-validation before delegating to TransactionService.
     */
    public Transaction withdraw(String accountId, double amount) {
        // Duplicated BR-001 guard — TransactionService will check again
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return transactionService.withdraw(accountId, amount);
    }

    /**
     * Transfer with duplicated pre-validation before delegating to TransactionService.
     */
    public Transaction transfer(String sourceAccountId, String destinationAccountId, double amount) {
        // Duplicated BR-001 guard — TransactionService will check again
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        return transactionService.transfer(sourceAccountId, destinationAccountId, amount);
    }

    // ---- Reporting ----

    public List<Transaction> getTransactionsForAccount(String accountId) {
        return reportService.getTransactionsForAccount(accountId);
    }

    public void printAccountStatement(String accountId) {
        reportService.printAccountStatement(accountId);
    }
}
