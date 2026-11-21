# Transformation Target

This document defines the **approved, bounded modernisation task** for IBM Bob to execute
against the legacy banking monolith. It is the single source of truth for what is in and
out of scope during the live demo.

---

## Task Title

> **Refactor Transaction Processing into a clearly separated capability while preserving behavior.**

---

## Approved Changes

### 1. Create a dedicated transaction-processing package

Create `com.bobstorm.banking.transaction` (or `transactionprocessing`) with a clear public
interface. The package should own:

- Transaction orchestration logic (deposit, withdraw, transfer flows)
- Business rule enforcement (BR-001 through BR-006)
- Transaction record creation and status assignment

### 2. Move transaction orchestration out of mixed-concern methods

Extract the validation, balance-mutation, record-creation, and status-setting steps from
`TransactionService`'s god-methods into focused collaborators or a clean orchestrator class
within the new package.

### 3. Introduce a persistence interface (if feasible)

Introduce a small `BankRepository` interface that `InMemoryBankRepository` implements.
Services in the new package should depend on the interface, not the concrete class.
Skip this step if it requires changes outside the transaction-processing scope.

### 4. Preserve all documented business rules

Business rules BR-001 through BR-006 must continue to pass exactly as they do in the
baseline. BR-007 (approval threshold) should also be preserved if it was included.

### 5. Add or improve regression tests

Extend `TransactionServiceTest` (or add a new `TransactionProcessorTest`) to cover the
refactored capability. All baseline tests in `BankingServiceTest`, `TransactionServiceTest`,
and `ReportServiceTest` must continue to pass.

---

## Out of Scope

The following changes are **explicitly prohibited** for this transformation:

| Prohibited action | Reason |
|-------------------|--------|
| Rewriting in another language or framework | Scope guardrail |
| Adding a database, message broker, or cloud deployment | Scope guardrail |
| Changing or inventing business rules | Accuracy of demo |
| Deleting tests to make builds pass | Integrity of baseline |
| Unrelated formatting, renaming, or mass changes | Keeps diff reviewable |
| Changes to Customer or Reporting behaviour | Out of scope |
| Full microservice infrastructure | Scope guardrail |

---

## Constraints for the Agent

1. Do not rewrite; extract and reorganise.
2. Keep the application buildable and runnable (`mvn clean package`, `java -jar …`).
3. If safe extraction cannot be completed, explain and implement the smallest safe refactor.
4. Provide a changed-file summary and rationale after completion.
5. Run `mvn test` and report any failures.

---

## Post-Transformation Validation Checklist

- [ ] `mvn clean package` succeeds.
- [ ] All baseline tests still pass (`mvn test` — zero failures, zero errors).
- [ ] New/updated regression tests cover the refactored transaction capability.
- [ ] Transaction capability has a distinct package or interface boundary.
- [ ] `git diff` is captured and reviewable.
- [ ] No unrelated changes beyond the approved scope.
- [ ] Modernised repository can be packaged as a ZIP for sharing.

---

## Expected Analysis Findings (evidence-backed)

When IBM Bob analyses this repository before executing the transformation it should report:

| Finding | Evidence location |
|---------|------------------|
| Technology: Java 11, Maven, JUnit 4 | `pom.xml` |
| Capabilities: Customer, Account, Transaction, Reporting | `service/` package |
| Architecture: monolith, no interface abstractions | `InMemoryBankRepository`, service constructors |
| Business rules: BR-001 – BR-006 | `TransactionService.java` comments |
| Hotspot: `TransactionService` has multiple responsibilities | `TransactionService.java` — god methods |
| Recommendation: isolate transaction processing | This document |
