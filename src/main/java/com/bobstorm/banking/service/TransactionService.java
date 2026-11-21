package com.bobstorm.banking.service;

import com.bobstorm.banking.model.Account;
import com.bobstorm.banking.model.Transaction;
import com.bobstorm.banking.repository.InMemoryBankRepository;

import java.util.UUID;

/**
 * Processes deposits, withdrawals, and transfers.
 *
 * =============================================================
 * INTENTIONAL LEGACY CHARACTERISTICS — do not "fix" before demo
 * =============================================================
 * 1. God-method: each of deposit/withdraw/transfer does validation,
 *    account lookup, balance mutation, transaction creation, status
 *    setting, and persistence all in a single method body.
 * 2. Direct concrete dependency on InMemoryBankRepository (no interface).
 * 3. Validation logic (amount > 0, active account, sufficient balance)
 *    is duplicated here AND in AccountService AND in ValidationUtil.
 * 4. System.out.println used for "logging" rather than a proper logger.
 * 5. Transfer atomicity (BR-005) is guarded by a manual try/catch,
 *    not a proper unit-of-work/transaction pattern.
 * =============================================================
 */
public class TransactionService {

    // Approval threshold for BR-007 (optional). Amounts above this become PENDING_APPROVAL.
    // Set to 0 or negative to disable.
    private static final double APPROVAL_THRESHOLD = 10_000.0;

    // Legacy: direct concrete dependency — no interface abstraction
    private InMemoryBankRepository repository;

    public TransactionService(InMemoryBankRepository repository) {
        this.repository = repository;
    }

    /**
     * Processes a deposit onto the given account.
     *
     * Business rules applied:
     *   BR-001 — amount must be positive.
     *   BR-003 — account must be ACTIVE.
     *   BR-006 — sets COMPLETED or REJECTED status.
     *   BR-007 — amounts above threshold become PENDING_APPROVAL.
     *
     * @return the persisted Transaction record
     */
    public Transaction deposit(String accountId, double amount) {
        String txId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(txId, accountId, null, amount, Transaction.Type.DEPOSIT);

        // BR-001: duplicated validation (also in AccountService and ValidationUtil)
        if (amount <= 0) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-001: Deposit amount must be greater than zero");
            repository.saveTransaction(tx);
            System.out.println("REJECTED deposit " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        Account account = repository.findAccountById(accountId);

        // BR-003: duplicated active-account check
        if (account == null || !Account.Status.ACTIVE.equals(account.getStatus())) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-003: Account is not active or does not exist: " + accountId);
            repository.saveTransaction(tx);
            System.out.println("REJECTED deposit " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        // BR-007: optional approval threshold
        if (APPROVAL_THRESHOLD > 0 && amount > APPROVAL_THRESHOLD) {
            tx.setStatus(Transaction.Status.PENDING_APPROVAL);
            repository.saveTransaction(tx);
            System.out.println("PENDING_APPROVAL deposit " + txId + " amount=" + amount);
            return tx;
        }

        // Apply balance mutation directly (mixing concerns)
        account.setBalance(account.getBalance() + amount);
        repository.saveAccount(account);

        tx.setStatus(Transaction.Status.COMPLETED);
        repository.saveTransaction(tx);
        System.out.println("COMPLETED deposit " + txId + " amount=" + amount
                + " newBalance=" + account.getBalance());
        return tx;
    }

    /**
     * Processes a withdrawal from the given account.
     *
     * Business rules applied:
     *   BR-001 — amount must be positive.
     *   BR-002 — sufficient balance.
     *   BR-003 — account must be ACTIVE.
     *   BR-006 — COMPLETED or REJECTED.
     *   BR-007 — amounts above threshold become PENDING_APPROVAL.
     *
     * @return the persisted Transaction record
     */
    public Transaction withdraw(String accountId, double amount) {
        String txId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(txId, accountId, null, amount, Transaction.Type.WITHDRAWAL);

        // BR-001: duplicated validation
        if (amount <= 0) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-001: Withdrawal amount must be greater than zero");
            repository.saveTransaction(tx);
            System.out.println("REJECTED withdrawal " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        Account account = repository.findAccountById(accountId);

        // BR-003: duplicated active-account check
        if (account == null || !Account.Status.ACTIVE.equals(account.getStatus())) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-003: Account is not active or does not exist: " + accountId);
            repository.saveTransaction(tx);
            System.out.println("REJECTED withdrawal " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        // BR-002: duplicated balance check
        if (account.getBalance() < amount) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-002: Insufficient balance. Available: " + account.getBalance());
            repository.saveTransaction(tx);
            System.out.println("REJECTED withdrawal " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        // BR-007: optional approval threshold
        if (APPROVAL_THRESHOLD > 0 && amount > APPROVAL_THRESHOLD) {
            tx.setStatus(Transaction.Status.PENDING_APPROVAL);
            repository.saveTransaction(tx);
            System.out.println("PENDING_APPROVAL withdrawal " + txId + " amount=" + amount);
            return tx;
        }

        // Apply balance mutation directly (mixing concerns)
        account.setBalance(account.getBalance() - amount);
        repository.saveAccount(account);

        tx.setStatus(Transaction.Status.COMPLETED);
        repository.saveTransaction(tx);
        System.out.println("COMPLETED withdrawal " + txId + " amount=" + amount
                + " newBalance=" + account.getBalance());
        return tx;
    }

    /**
     * Processes a transfer between two distinct accounts.
     *
     * Business rules applied:
     *   BR-001 — amount must be positive.
     *   BR-002 — source balance must be sufficient.
     *   BR-003 — source account must be ACTIVE.
     *   BR-004 — source and destination must be distinct existing accounts.
     *   BR-005 — atomicity: if destination credit fails, source debit is rolled back.
     *   BR-006 — COMPLETED or REJECTED.
     *   BR-007 — amounts above threshold become PENDING_APPROVAL.
     *
     * @return the persisted Transaction record
     */
    public Transaction transfer(String sourceAccountId, String destinationAccountId, double amount) {
        String txId = UUID.randomUUID().toString();
        Transaction tx = new Transaction(txId, sourceAccountId, destinationAccountId,
                amount, Transaction.Type.TRANSFER);

        // BR-001: duplicated validation
        if (amount <= 0) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-001: Transfer amount must be greater than zero");
            repository.saveTransaction(tx);
            System.out.println("REJECTED transfer " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        // BR-004: source and destination must be distinct
        if (sourceAccountId == null || sourceAccountId.equals(destinationAccountId)) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-004: Transfer requires two distinct existing accounts");
            repository.saveTransaction(tx);
            System.out.println("REJECTED transfer " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        Account source = repository.findAccountById(sourceAccountId);
        Account destination = repository.findAccountById(destinationAccountId);

        // BR-004: both accounts must exist
        if (source == null || destination == null) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-004: One or both accounts do not exist");
            repository.saveTransaction(tx);
            System.out.println("REJECTED transfer " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        // BR-003: source must be active (spec only requires source to be active for processing)
        if (!Account.Status.ACTIVE.equals(source.getStatus())) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-003: Source account is not active: " + sourceAccountId);
            repository.saveTransaction(tx);
            System.out.println("REJECTED transfer " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        // BR-002: duplicated balance check
        if (source.getBalance() < amount) {
            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-002: Insufficient balance. Available: " + source.getBalance());
            repository.saveTransaction(tx);
            System.out.println("REJECTED transfer " + txId + ": " + tx.getRejectionReason());
            return tx;
        }

        // BR-007: optional approval threshold
        if (APPROVAL_THRESHOLD > 0 && amount > APPROVAL_THRESHOLD) {
            tx.setStatus(Transaction.Status.PENDING_APPROVAL);
            repository.saveTransaction(tx);
            System.out.println("PENDING_APPROVAL transfer " + txId + " amount=" + amount);
            return tx;
        }

        // BR-005: manual atomicity guard — debit source first, credit destination,
        // roll back source debit if credit fails
        double originalSourceBalance = source.getBalance();
        try {
            source.setBalance(source.getBalance() - amount);
            repository.saveAccount(source);

            destination.setBalance(destination.getBalance() + amount);
            repository.saveAccount(destination);
        } catch (Exception e) {
            // BR-005: roll back source debit
            source.setBalance(originalSourceBalance);
            repository.saveAccount(source);

            tx.setStatus(Transaction.Status.REJECTED);
            tx.setRejectionReason("BR-005: Transfer rolled back due to error: " + e.getMessage());
            repository.saveTransaction(tx);
            System.out.println("REJECTED transfer " + txId + " (rolled back): " + e.getMessage());
            return tx;
        }

        tx.setStatus(Transaction.Status.COMPLETED);
        repository.saveTransaction(tx);
        System.out.println("COMPLETED transfer " + txId + " amount=" + amount
                + " from=" + sourceAccountId + " to=" + destinationAccountId);
        return tx;
    }
}
