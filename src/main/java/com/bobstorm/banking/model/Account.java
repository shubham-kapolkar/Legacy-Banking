package com.bobstorm.banking.model;

/**
 * Represents a bank account.
 * Legacy-style: mutable fields, direct balance mutation exposed.
 */
public class Account {

    public enum Status {
        ACTIVE, INACTIVE, CLOSED
    }

    private String accountId;
    private String customerId;
    private double balance;
    private Status status;

    public Account(String accountId, String customerId, double initialBalance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.balance = initialBalance;
        this.status = Status.ACTIVE;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public double getBalance() {
        return balance;
    }

    // Legacy: balance mutation is directly exposed rather than domain-event-driven
    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Account{accountId='" + accountId + "', customerId='" + customerId
                + "', balance=" + balance + ", status=" + status + "}";
    }
}
