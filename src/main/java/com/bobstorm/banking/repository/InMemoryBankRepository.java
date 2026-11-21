package com.bobstorm.banking.repository;

import com.bobstorm.banking.model.Account;
import com.bobstorm.banking.model.Customer;
import com.bobstorm.banking.model.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory storage for all banking domain objects.
 * Legacy characteristic: concrete class depended on directly by services;
 * no repository interface/abstraction.
 */
public class InMemoryBankRepository {

    private final Map<String, Customer> customers = new HashMap<>();
    private final Map<String, Account> accounts = new HashMap<>();
    private final Map<String, Transaction> transactions = new HashMap<>();

    // ---- Customer ----

    public void saveCustomer(Customer customer) {
        customers.put(customer.getCustomerId(), customer);
    }

    public Customer findCustomerById(String customerId) {
        return customers.get(customerId);
    }

    public List<Customer> findAllCustomers() {
        return new ArrayList<>(customers.values());
    }

    // ---- Account ----

    public void saveAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }

    public Account findAccountById(String accountId) {
        return accounts.get(accountId);
    }

    public List<Account> findAllAccounts() {
        return new ArrayList<>(accounts.values());
    }

    // ---- Transaction ----

    public void saveTransaction(Transaction transaction) {
        transactions.put(transaction.getTransactionId(), transaction);
    }

    public Transaction findTransactionById(String transactionId) {
        return transactions.get(transactionId);
    }

    public List<Transaction> findTransactionsByAccountId(String accountId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions.values()) {
            if (accountId.equals(t.getSourceAccountId())
                    || accountId.equals(t.getDestinationAccountId())) {
                result.add(t);
            }
        }
        return result;
    }

    public List<Transaction> findAllTransactions() {
        return new ArrayList<>(transactions.values());
    }
}
