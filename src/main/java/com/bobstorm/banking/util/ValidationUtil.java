package com.bobstorm.banking.util;

import com.bobstorm.banking.model.Account;

/**
 * Shared validation helpers.
 * Legacy characteristic: validation logic is still duplicated in service classes
 * despite this utility existing — services each call their own inline checks too.
 */
public class ValidationUtil {

    private ValidationUtil() {
        // utility class
    }

    /**
     * Returns true if the amount is strictly positive (BR-001).
     */
    public static boolean isPositiveAmount(double amount) {
        return amount > 0;
    }

    /**
     * Returns true if the account exists and is ACTIVE (BR-003).
     */
    public static boolean isAccountActive(Account account) {
        return account != null && Account.Status.ACTIVE.equals(account.getStatus());
    }

    /**
     * Returns true if the account has at least the requested amount (BR-002).
     */
    public static boolean hasSufficientBalance(Account account, double amount) {
        return account != null && account.getBalance() >= amount;
    }
}
