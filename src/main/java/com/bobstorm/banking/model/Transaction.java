package com.bobstorm.banking.model;

import java.time.LocalDateTime;

/**
 * Represents a bank transaction record.
 * Legacy-style: mutable fields, status set externally after creation.
 */
public class Transaction {

    public enum Type {
        DEPOSIT, WITHDRAWAL, TRANSFER
    }

    public enum Status {
        COMPLETED, REJECTED, PENDING_APPROVAL
    }

    private String transactionId;
    private String sourceAccountId;
    private String destinationAccountId; // null for deposits/withdrawals
    private double amount;
    private Type type;
    private Status status;
    private LocalDateTime createdAt;
    private String rejectionReason;

    public Transaction(String transactionId, String sourceAccountId,
                       String destinationAccountId, double amount, Type type) {
        this.transactionId = transactionId;
        this.sourceAccountId = sourceAccountId;
        this.destinationAccountId = destinationAccountId;
        this.amount = amount;
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSourceAccountId() {
        return sourceAccountId;
    }

    public void setSourceAccountId(String sourceAccountId) {
        this.sourceAccountId = sourceAccountId;
    }

    public String getDestinationAccountId() {
        return destinationAccountId;
    }

    public void setDestinationAccountId(String destinationAccountId) {
        this.destinationAccountId = destinationAccountId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    @Override
    public String toString() {
        return "Transaction{id='" + transactionId + "', type=" + type
                + ", amount=" + amount + ", status=" + status
                + ", source='" + sourceAccountId + "', dest='" + destinationAccountId + "'}";
    }
}
