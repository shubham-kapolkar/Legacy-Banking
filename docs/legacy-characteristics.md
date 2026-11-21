# Legacy Characteristics

This document records the **intentional** design flaws present in the baseline codebase.
They are here by design to serve as realistic modernisation targets — not bugs to fix before demo.

---

## LC-001 — TransactionService is a God Class

**File:** `TransactionService.java`

Each of `deposit()`, `withdraw()`, and `transfer()` performs all of the following in a single
method body:

1. Input validation (BR-001)
2. Account lookup
3. Business rule checks (BR-002, BR-003, BR-004)
4. Balance mutation
5. Transaction record creation
6. Status assignment (BR-006)
7. Persistence
8. Console logging

This violates the Single Responsibility Principle and is the primary modernisation hotspot.

---

## LC-002 — Direct Concrete Repository Dependency

**Files:** `AccountService.java`, `TransactionService.java`, `ReportService.java`

All three service classes declare a field of type `InMemoryBankRepository` directly.
There is no repository interface or abstraction. Swapping the persistence layer requires
modifying all service classes.

---

## LC-003 — Duplicated Validation Logic

**Files:** `AccountService.java`, `TransactionService.java`, `BankingService.java`,
`ValidationUtil.java`

The same guards (amount > 0, account is ACTIVE, balance is sufficient) are implemented
independently in:
- `AccountService.deposit()` / `AccountService.withdraw()`
- `TransactionService.deposit()` / `TransactionService.withdraw()` / `TransactionService.transfer()`
- `BankingService.deposit()` / `BankingService.withdraw()` / `BankingService.transfer()`
- `ValidationUtil` (utility exists but services do not consistently use it)

---

## LC-004 — Inconsistent Service Delegation in the Facade

**File:** `BankingService.java`

`BankingService` delegates customer/account management to `AccountService`, and
deposit/withdraw/transfer to `TransactionService`. However, `AccountService` also exposes
its own `deposit()` and `withdraw()` methods that mutate balances *without* creating a
`Transaction` record — bypassing the audit trail. External callers using `BankingService`
get the correct path; callers using `AccountService` directly do not.

---

## LC-005 — Console Logging in Service Layer

**File:** `TransactionService.java`, `ReportService.java`

`System.out.println` is used for status output and reporting. There is no structured logging,
no log levels, and no way to suppress or redirect output in tests.

---

## LC-006 — Mutable Models with Public Setters

**Files:** `Account.java`, `Customer.java`, `Transaction.java`

All model fields expose public setters. Any layer can mutate an account's balance or change a
transaction's status after the fact, making audit-trail integrity easy to break accidentally.

---

## LC-007 — Internal Object Construction in BankingService

**File:** `BankingService.java`

`BankingService` instantiates `InMemoryBankRepository`, `AccountService`,
`TransactionService`, and `ReportService` in its constructor. There is no dependency injection.
This makes unit-testing `BankingService` in isolation impossible without partial mocking hacks.

---

## LC-008 — Manual Atomicity Guard

**File:** `TransactionService.java` — `transfer()`

Transfer rollback is implemented as a manual `try/catch` that resets the source account's
balance. This pattern is fragile: a second failure during rollback could leave the system in
an inconsistent state, and there is no equivalent guard on the destination account's prior
state.

---

## Summary Table

| ID     | Location              | Category                        |
|--------|-----------------------|---------------------------------|
| LC-001 | TransactionService    | God class / mixed concerns      |
| LC-002 | All services          | Tight coupling (no interface)   |
| LC-003 | All services + util   | Duplicated validation           |
| LC-004 | BankingService        | Inconsistent delegation         |
| LC-005 | TransactionService    | I/O mixed into service layer    |
| LC-006 | All models            | Mutable / low encapsulation     |
| LC-007 | BankingService        | No dependency injection         |
| LC-008 | TransactionService    | Fragile manual atomicity        |
