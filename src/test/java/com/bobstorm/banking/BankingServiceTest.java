package com.bobstorm.banking;

import com.bobstorm.banking.model.Account;
import com.bobstorm.banking.model.Transaction;
import com.bobstorm.banking.service.BankingService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Integration-level tests exercising the full BankingService facade.
 * Covers the baseline requirements from the spec (sections 5 and 8).
 */
public class BankingServiceTest {

    private BankingService bank;

    @Before
    public void setUp() {
        bank = new BankingService();
        bank.createCustomer("C001", "Alice Smith", "alice@example.com", "555-0101");
        bank.createCustomer("C002", "Bob Jones",   "bob@example.com",   "555-0202");
        bank.createAccount("A001", "C001", 1000.00);
        bank.createAccount("A002", "C002",  500.00);
    }

    // --- Deposit tests ---

    /** Spec §8: Valid deposit increases balance. */
    @Test
    public void testValidDepositIncreasesBalance() {
        Transaction tx = bank.deposit("A001", 200.00);
        assertEquals(Transaction.Status.COMPLETED, tx.getStatus());
        assertEquals(1200.00, bank.getAccount("A001").getBalance(), 0.001);
    }

    /** Spec §8: Zero deposit is rejected. BR-001. */
    @Test
    public void testZeroDepositIsRejected() {
        try {
            bank.deposit("A001", 0);
            fail("Expected IllegalArgumentException for zero amount");
        } catch (IllegalArgumentException e) {
            // BankingService facade throws before reaching TransactionService
        }
        // Balance unchanged
        assertEquals(1000.00, bank.getAccount("A001").getBalance(), 0.001);
    }

    /** Spec §8: Negative deposit is rejected. BR-001. */
    @Test
    public void testNegativeDepositIsRejected() {
        try {
            bank.deposit("A001", -50.00);
            fail("Expected IllegalArgumentException for negative amount");
        } catch (IllegalArgumentException e) {
            // expected
        }
        assertEquals(1000.00, bank.getAccount("A001").getBalance(), 0.001);
    }

    // --- Withdrawal tests ---

    /** Spec §8: Withdrawal with sufficient funds succeeds. BR-002. */
    @Test
    public void testWithdrawWithSufficientFundsSucceeds() {
        Transaction tx = bank.withdraw("A001", 400.00);
        assertEquals(Transaction.Status.COMPLETED, tx.getStatus());
        assertEquals(600.00, bank.getAccount("A001").getBalance(), 0.001);
    }

    /** Spec §8: Withdrawal exceeding balance is rejected; balance unchanged. BR-002. */
    @Test
    public void testWithdrawExceedingBalanceIsRejected() {
        Transaction tx = bank.withdraw("A001", 1500.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertNotNull(tx.getRejectionReason());
        // Balance must be unchanged
        assertEquals(1000.00, bank.getAccount("A001").getBalance(), 0.001);
    }

    // --- Transfer tests ---

    /** Spec §8: Transfer between accounts updates both balances. BR-004. */
    @Test
    public void testTransferUpdatesBothBalances() {
        Transaction tx = bank.transfer("A001", "A002", 300.00);
        assertEquals(Transaction.Status.COMPLETED, tx.getStatus());
        assertEquals(700.00,  bank.getAccount("A001").getBalance(), 0.001);
        assertEquals(800.00,  bank.getAccount("A002").getBalance(), 0.001);
    }

    /** Spec §8: Transfer to same account is rejected. BR-004. */
    @Test
    public void testTransferToSameAccountIsRejected() {
        Transaction tx = bank.transfer("A001", "A001", 100.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertNotNull(tx.getRejectionReason());
        // Balance unchanged
        assertEquals(1000.00, bank.getAccount("A001").getBalance(), 0.001);
    }

    /** Spec §8: Invalid (zero) transfer amount is rejected. BR-001. */
    @Test
    public void testInvalidTransferAmountIsRejected() {
        try {
            bank.transfer("A001", "A002", 0);
            fail("Expected IllegalArgumentException for zero amount");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // --- Account status tests ---

    /** Spec §8: Inactive source account cannot transact. BR-003. */
    @Test
    public void testInactiveAccountCannotTransact() {
        bank.deactivateAccount("A001");
        Transaction tx = bank.deposit("A001", 100.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertNotNull(tx.getRejectionReason());
    }

    // --- Transaction record persistence ---

    /** Spec §8: Transaction status and record are persisted for success. BR-006. */
    @Test
    public void testCompletedTransactionIsPersisted() {
        Transaction tx = bank.deposit("A001", 50.00);
        assertEquals(Transaction.Status.COMPLETED, tx.getStatus());
        assertNotNull(tx.getTransactionId());
        // The transaction must appear in the account's history
        boolean found = bank.getTransactionsForAccount("A001")
                .stream().anyMatch(t -> t.getTransactionId().equals(tx.getTransactionId()));
        assertTrue("Completed transaction should be in account history", found);
    }

    /** Spec §8: Transaction status and record are persisted for rejection. BR-006. */
    @Test
    public void testRejectedTransactionIsPersisted() {
        Transaction tx = bank.withdraw("A001", 9999.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertNotNull(tx.getRejectionReason());
        boolean found = bank.getTransactionsForAccount("A001")
                .stream().anyMatch(t -> t.getTransactionId().equals(tx.getTransactionId()));
        assertTrue("Rejected transaction should be in account history", found);
    }
}
