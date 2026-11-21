package com.bobstorm.banking.service;

import com.bobstorm.banking.model.Account;
import com.bobstorm.banking.model.Customer;
import com.bobstorm.banking.repository.InMemoryBankRepository;

/**
 * Handles account lifecycle and simple balance operations.
 *
 * Legacy characteristics:
 * - Depends directly on the concrete InMemoryBankRepository (no interface).
 * - Duplicates amount/balance validation that also exists in TransactionService
 *   and ValidationUtil — intentional overlap to reflect real legacy codebases.
 * - Deposit and withdrawal here are "low-level" helpers; BankingService also
 *   calls TransactionService for the same operations, creating inconsistency.
 */
public class AccountService {

    // Legacy: direct concrete dependency — no interface
    private InMemoryBankRepository repository;

    public AccountService(InMemoryBankRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and persists a new customer.
     */
    public Customer createCustomer(String customerId, String name, String email, String phone) {
        Customer customer = new Customer(customerId, name, email, phone);
        repository.saveCustomer(customer);
        return customer;
    }

    /**
     * Creates and persists a new account for an existing customer with an initial balance.
     */
    public Account createAccount(String accountId, String customerId, double initialBalance) {
        // Duplicated validation — also checked in TransactionService (legacy smell)
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        Account account = new Account(accountId, customerId, initialBalance);
        repository.saveAccount(account);
        return account;
    }

    /**
     * Retrieves an account by ID; returns null if not found.
     */
    public Account getAccount(String accountId) {
        return repository.findAccountById(accountId);
    }

    /**
     * Retrieves a customer by ID; returns null if not found.
     */
    public Customer getCustomer(String customerId) {
        return repository.findCustomerById(customerId);
    }

    /**
     * Direct deposit — adds amount to account balance without creating a Transaction record.
     * Used internally; BankingService routes external deposits through TransactionService.
     * This inconsistency is an intentional legacy characteristic.
     *
     * BR-001: amount must be positive.
     * BR-003: account must be ACTIVE.
     */
    public void deposit(String accountId, double amount) {
        // Duplicated validation (BR-001) — also in TransactionService
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }
        Account account = repository.findAccountById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        // Duplicated active-account check (BR-003) — also in TransactionService
        if (!Account.Status.ACTIVE.equals(account.getStatus())) {
            throw new IllegalStateException("Account is not active: " + accountId);
        }
        account.setBalance(account.getBalance() + amount);
        repository.saveAccount(account);
    }

    /**
     * Direct withdrawal — subtracts amount from account balance without a Transaction record.
     * Same inconsistency as deposit().
     *
     * BR-001: amount must be positive.
     * BR-002: balance must be sufficient.
     * BR-003: account must be ACTIVE.
     */
    public void withdraw(String accountId, double amount) {
        // Duplicated validation (BR-001) — also in TransactionService
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }
        Account account = repository.findAccountById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        // Duplicated active-account check (BR-003)
        if (!Account.Status.ACTIVE.equals(account.getStatus())) {
            throw new IllegalStateException("Account is not active: " + accountId);
        }
        // Duplicated balance check (BR-002)
        if (account.getBalance() < amount) {
            throw new IllegalStateException("Insufficient funds in account: " + accountId);
        }
        account.setBalance(account.getBalance() - amount);
        repository.saveAccount(account);
    }

    /**
     * Deactivates an account.
     */
    public void deactivateAccount(String accountId) {
        Account account = repository.findAccountById(accountId);
        if (account == null) {
            throw new IllegalArgumentException("Account not found: " + accountId);
        }
        account.setStatus(Account.Status.INACTIVE);
        repository.saveAccount(account);
    }
}
