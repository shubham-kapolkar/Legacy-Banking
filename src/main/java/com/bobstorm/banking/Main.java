package com.bobstorm.banking;

import com.bobstorm.banking.model.Account;
import com.bobstorm.banking.model.Transaction;
import com.bobstorm.banking.service.BankingService;

/**
 * Entry point that exercises the full banking workflow via BankingService.
 * Demonstrates: create customer/account, deposit, withdraw, transfer, and reporting.
 */
public class Main {

    public static void main(String[] args) {
        BankingService bank = new BankingService();

        // Setup: two customers and accounts
        bank.createCustomer("C001", "Alice Smith", "alice@example.com", "555-0101");
        bank.createCustomer("C002", "Bob Jones",  "bob@example.com",   "555-0202");

        Account aliceAccount = bank.createAccount("A001", "C001", 500.00);
        Account bobAccount   = bank.createAccount("A002", "C002", 200.00);

        System.out.println("--- Initial state ---");
        bank.printAccountStatement("A001");
        bank.printAccountStatement("A002");

        // Deposit into Alice's account
        Transaction depositTx = bank.deposit("A001", 300.00);
        System.out.println("Deposit result: " + depositTx.getStatus());

        // Withdraw from Alice
        Transaction withdrawTx = bank.withdraw("A001", 100.00);
        System.out.println("Withdraw result: " + withdrawTx.getStatus());

        // Transfer from Alice to Bob
        Transaction transferTx = bank.transfer("A001", "A002", 250.00);
        System.out.println("Transfer result: " + transferTx.getStatus());

        // Attempt overdraft
        Transaction overdraftTx = bank.withdraw("A001", 999.00);
        System.out.println("Overdraft result: " + overdraftTx.getStatus()
                + " — " + overdraftTx.getRejectionReason());

        System.out.println("\n--- Final state ---");
        bank.printAccountStatement("A001");
        bank.printAccountStatement("A002");
    }
}
