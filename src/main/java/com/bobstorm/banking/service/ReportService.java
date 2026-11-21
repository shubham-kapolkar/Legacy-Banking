package com.bobstorm.banking.service;

import com.bobstorm.banking.model.Account;
import com.bobstorm.banking.model.Transaction;
import com.bobstorm.banking.repository.InMemoryBankRepository;

import java.util.List;

/**
 * Provides account transaction history and simple summaries.
 *
 * Legacy characteristic: depends directly on InMemoryBankRepository (no interface).
 */
public class ReportService {

    // Legacy: direct concrete dependency
    private InMemoryBankRepository repository;

    public ReportService(InMemoryBankRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns all transactions associated with the given account ID
     * (as either source or destination).
     */
    public List<Transaction> getTransactionsForAccount(String accountId) {
        return repository.findTransactionsByAccountId(accountId);
    }

    /**
     * Prints a simple text summary of an account's transaction history to stdout.
     * Legacy characteristic: mixing I/O concern into service layer.
     */
    public void printAccountStatement(String accountId) {
        Account account = repository.findAccountById(accountId);
        if (account == null) {
            System.out.println("Account not found: " + accountId);
            return;
        }

        List<Transaction> transactions = repository.findTransactionsByAccountId(accountId);

        System.out.println("=== Account Statement ===");
        System.out.println("Account : " + accountId);
        System.out.println("Customer: " + account.getCustomerId());
        System.out.println("Balance : " + account.getBalance());
        System.out.println("Status  : " + account.getStatus());
        System.out.println("Transactions (" + transactions.size() + "):");
        for (Transaction t : transactions) {
            System.out.println("  [" + t.getCreatedAt() + "] "
                    + t.getType() + " " + t.getAmount()
                    + " -> " + t.getStatus()
                    + (t.getRejectionReason() != null ? " (" + t.getRejectionReason() + ")" : ""));
        }
        System.out.println("=========================");
    }

    /**
     * Returns the total amount of COMPLETED transactions of a given type for an account.
     */
    public double getTotalCompletedAmount(String accountId, Transaction.Type type) {
        List<Transaction> transactions = repository.findTransactionsByAccountId(accountId);
        double total = 0.0;
        for (Transaction t : transactions) {
            if (type.equals(t.getType()) && Transaction.Status.COMPLETED.equals(t.getStatus())) {
                total += t.getAmount();
            }
        }
        return total;
    }
}
