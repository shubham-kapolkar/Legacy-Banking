package com.bobstorm.banking;

import com.bobstorm.banking.model.Account;
import com.bobstorm.banking.model.Transaction;
import com.bobstorm.banking.repository.InMemoryBankRepository;
import com.bobstorm.banking.service.AccountService;
import com.bobstorm.banking.service.TransactionService;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit-level tests focused specifically on TransactionService business rules.
 * Uses TransactionService directly to test each business rule in isolation.
 */
public class TransactionServiceTest {

    private InMemoryBankRepository repository;
    private AccountService accountService;
    private TransactionService transactionService;

    @Before
    public void setUp() {
        repository = new InMemoryBankRepository();
        accountService = new AccountService(repository);
        transactionService = new TransactionService(repository);

        accountService.createCustomer("C001", "Alice", "alice@example.com", "555-0101");
        accountService.createCustomer("C002", "Bob",   "bob@example.com",   "555-0202");
        accountService.createAccount("A001", "C001", 1000.00);
        accountService.createAccount("A002", "C002",  500.00);
    }

    // --- BR-001: Positive amount ---

    /** BR-001: Deposit of zero is recorded as REJECTED. */
    @Test
    public void testDepositZeroAmountIsRejected() {
        Transaction tx = transactionService.deposit("A001", 0.0);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-001"));
    }

    /** BR-001: Deposit of negative amount is recorded as REJECTED. */
    @Test
    public void testDepositNegativeAmountIsRejected() {
        Transaction tx = transactionService.deposit("A001", -100.0);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-001"));
    }

    /** BR-001: Withdrawal of zero amount is recorded as REJECTED. */
    @Test
    public void testWithdrawZeroAmountIsRejected() {
        Transaction tx = transactionService.withdraw("A001", 0.0);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-001"));
    }

    /** BR-001: Transfer of zero amount is recorded as REJECTED. */
    @Test
    public void testTransferZeroAmountIsRejected() {
        Transaction tx = transactionService.transfer("A001", "A002", 0.0);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-001"));
    }

    // --- BR-002: Sufficient balance ---

    /** BR-002: Withdrawal exceeding balance leaves balance unchanged. */
    @Test
    public void testWithdrawExceedingBalanceLeavesBalanceUnchanged() {
        Transaction tx = transactionService.withdraw("A001", 2000.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-002"));
        assertEquals(1000.00, repository.findAccountById("A001").getBalance(), 0.001);
    }

    /** BR-002: Transfer exceeding source balance is rejected; both balances unchanged. */
    @Test
    public void testTransferExceedingSourceBalanceIsRejected() {
        Transaction tx = transactionService.transfer("A001", "A002", 5000.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-002"));
        assertEquals(1000.00, repository.findAccountById("A001").getBalance(), 0.001);
        assertEquals( 500.00, repository.findAccountById("A002").getBalance(), 0.001);
    }

    // --- BR-003: Active account ---

    /** BR-003: Deposit on inactive account is rejected. */
    @Test
    public void testDepositOnInactiveAccountIsRejected() {
        repository.findAccountById("A001").setStatus(Account.Status.INACTIVE);
        Transaction tx = transactionService.deposit("A001", 100.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-003"));
    }

    /** BR-003: Withdrawal on inactive account is rejected. */
    @Test
    public void testWithdrawOnInactiveAccountIsRejected() {
        repository.findAccountById("A001").setStatus(Account.Status.INACTIVE);
        Transaction tx = transactionService.withdraw("A001", 100.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-003"));
    }

    /** BR-003: Transfer from inactive source account is rejected. */
    @Test
    public void testTransferFromInactiveSourceIsRejected() {
        repository.findAccountById("A001").setStatus(Account.Status.INACTIVE);
        Transaction tx = transactionService.transfer("A001", "A002", 100.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-003"));
    }

    // --- BR-004: Distinct accounts for transfer ---

    /** BR-004: Transfer to same account is rejected. */
    @Test
    public void testTransferToSameAccountIsRejected() {
        Transaction tx = transactionService.transfer("A001", "A001", 100.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-004"));
        // Balance unchanged
        assertEquals(1000.00, repository.findAccountById("A001").getBalance(), 0.001);
    }

    /** BR-004: Transfer to non-existent destination is rejected. */
    @Test
    public void testTransferToNonExistentDestinationIsRejected() {
        Transaction tx = transactionService.transfer("A001", "A999", 100.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertTrue(tx.getRejectionReason().contains("BR-004"));
    }

    // --- BR-006: Transaction status persisted ---

    /** BR-006: Successful deposit is COMPLETED and persisted. */
    @Test
    public void testSuccessfulDepositIsCompletedAndPersisted() {
        Transaction tx = transactionService.deposit("A001", 100.00);
        assertEquals(Transaction.Status.COMPLETED, tx.getStatus());
        assertNotNull(repository.findTransactionById(tx.getTransactionId()));
    }

    /** BR-006: Rejected withdrawal is REJECTED and persisted with a reason. */
    @Test
    public void testRejectedWithdrawalIsPersistedWithReason() {
        Transaction tx = transactionService.withdraw("A001", 9999.00);
        assertEquals(Transaction.Status.REJECTED, tx.getStatus());
        assertNotNull(tx.getRejectionReason());
        assertNotNull(repository.findTransactionById(tx.getTransactionId()));
    }

    // --- Complete happy-path scenarios ---

    /** Happy path: deposit then withdraw, check balances. */
    @Test
    public void testDepositThenWithdrawBalanceCorrect() {
        transactionService.deposit("A001", 500.00);
        Transaction wTx = transactionService.withdraw("A001", 200.00);
        assertEquals(Transaction.Status.COMPLETED, wTx.getStatus());
        assertEquals(1300.00, repository.findAccountById("A001").getBalance(), 0.001);
    }

    /** Happy path: transfer updates both account balances correctly. */
    @Test
    public void testSuccessfulTransferUpdatesBothBalances() {
        Transaction tx = transactionService.transfer("A001", "A002", 400.00);
        assertEquals(Transaction.Status.COMPLETED, tx.getStatus());
        assertEquals( 600.00, repository.findAccountById("A001").getBalance(), 0.001);
        assertEquals( 900.00, repository.findAccountById("A002").getBalance(), 0.001);
    }
}
