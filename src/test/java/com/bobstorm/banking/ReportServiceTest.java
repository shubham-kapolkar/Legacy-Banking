package com.bobstorm.banking;

import com.bobstorm.banking.model.Transaction;
import com.bobstorm.banking.repository.InMemoryBankRepository;
import com.bobstorm.banking.service.AccountService;
import com.bobstorm.banking.service.ReportService;
import com.bobstorm.banking.service.TransactionService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for ReportService — verifies that transaction history is correctly
 * returned and filtered by account.
 */
public class ReportServiceTest {

    private InMemoryBankRepository repository;
    private AccountService accountService;
    private TransactionService transactionService;
    private ReportService reportService;

    @Before
    public void setUp() {
        repository = new InMemoryBankRepository();
        accountService = new AccountService(repository);
        transactionService = new TransactionService(repository);
        reportService = new ReportService(repository);

        accountService.createCustomer("C001", "Alice", "alice@example.com", "555-0101");
        accountService.createCustomer("C002", "Bob",   "bob@example.com",   "555-0202");
        accountService.createAccount("A001", "C001", 1000.00);
        accountService.createAccount("A002", "C002",  500.00);
    }

    /** Report returns transactions for the requested account (spec §8). */
    @Test
    public void testGetTransactionsForAccountReturnsOnlyThatAccountTransactions() {
        transactionService.deposit("A001", 200.00);
        transactionService.deposit("A002", 100.00);  // should NOT appear in A001 report

        List<Transaction> a001Txns = reportService.getTransactionsForAccount("A001");
        assertEquals(1, a001Txns.size());
        assertEquals("A001", a001Txns.get(0).getSourceAccountId());
    }

    /** Transfer appears in both source and destination account reports. */
    @Test
    public void testTransferAppearsInBothAccountReports() {
        transactionService.transfer("A001", "A002", 100.00);

        List<Transaction> a001Txns = reportService.getTransactionsForAccount("A001");
        List<Transaction> a002Txns = reportService.getTransactionsForAccount("A002");

        assertEquals(1, a001Txns.size());
        assertEquals(1, a002Txns.size());
        assertEquals(a001Txns.get(0).getTransactionId(), a002Txns.get(0).getTransactionId());
    }

    /** Report for an account with no transactions returns an empty list. */
    @Test
    public void testEmptyHistoryForNewAccount() {
        List<Transaction> txns = reportService.getTransactionsForAccount("A001");
        assertNotNull(txns);
        assertTrue(txns.isEmpty());
    }

    /** Rejected transactions also appear in account history. */
    @Test
    public void testRejectedTransactionAppearsInHistory() {
        transactionService.withdraw("A001", 9999.00);  // will be rejected
        List<Transaction> txns = reportService.getTransactionsForAccount("A001");
        assertEquals(1, txns.size());
        assertEquals(Transaction.Status.REJECTED, txns.get(0).getStatus());
    }

    /** Multiple transactions accumulate in account history. */
    @Test
    public void testMultipleTransactionsAccumulate() {
        transactionService.deposit("A001", 100.00);
        transactionService.deposit("A001", 200.00);
        transactionService.withdraw("A001", 50.00);

        List<Transaction> txns = reportService.getTransactionsForAccount("A001");
        assertEquals(3, txns.size());
    }

    /** getTotalCompletedAmount returns correct sum for deposits. */
    @Test
    public void testTotalCompletedDepositAmount() {
        transactionService.deposit("A001", 300.00);
        transactionService.deposit("A001", 200.00);
        transactionService.withdraw("A001", 9999.00);  // rejected — should not count

        double total = reportService.getTotalCompletedAmount("A001", Transaction.Type.DEPOSIT);
        assertEquals(500.00, total, 0.001);
    }
}
