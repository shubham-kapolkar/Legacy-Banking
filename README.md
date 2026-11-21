# Legacy Banking Monolith

> Controlled demo repository for the **BobStorm Hackathon** — a deliberately legacy-style Java
> banking application used to showcase IBM Bob's AI-assisted modernisation workflow.

---

## Prerequisites

| Tool   | Minimum version |
|--------|----------------|
| Java   | 11             |
| Maven  | 3.6            |

Verify with:
```bash
java -version
mvn -version
```

---

## Build

```bash
cd legacy-banking-monolith
mvn clean package -q
```

A successful build produces `target/legacy-banking-monolith-1.0.0.jar`.

---

## Run Tests

```bash
mvn test
```

Expected output: **all tests pass** (zero failures, zero errors).

To run a specific test class:
```bash
mvn test -Dtest=TransactionServiceTest
```

---

## Run the Application

```bash
java -jar target/legacy-banking-monolith-1.0.0.jar
```

This exercises a scripted banking workflow (create accounts, deposit, withdraw, transfer,
attempt overdraft) and prints output to stdout. No database, UI, or external service required.

---

## Project Layout

```
legacy-banking-monolith/
├── pom.xml
├── README.md
├── docs/
│   ├── legacy-characteristics.md   ← intentional design flaws documented
│   └── transformation-target.md    ← approved modernisation scope
└── src/
    ├── main/java/com/bobstorm/banking/
    │   ├── Main.java
    │   ├── model/
    │   │   ├── Customer.java
    │   │   ├── Account.java
    │   │   └── Transaction.java
    │   ├── service/
    │   │   ├── BankingService.java       ← top-level facade
    │   │   ├── AccountService.java
    │   │   ├── TransactionService.java   ← hotspot: multiple responsibilities
    │   │   └── ReportService.java
    │   ├── repository/
    │   │   └── InMemoryBankRepository.java
    │   └── util/
    │       └── ValidationUtil.java
    └── test/java/com/bobstorm/banking/
        ├── BankingServiceTest.java
        ├── TransactionServiceTest.java
        └── ReportServiceTest.java
```

---

## Business Rules

| Rule   | Description |
|--------|-------------|
| BR-001 | Deposit, withdrawal, and transfer amounts must be **greater than zero**. |
| BR-002 | A withdrawal or transfer debit must not leave the source balance **negative**. |
| BR-003 | Transactions can only be processed from an **ACTIVE** account. |
| BR-004 | A transfer requires **two distinct existing accounts**. |
| BR-005 | **Transfer atomicity** — if destination credit fails, source debit is rolled back. |
| BR-006 | Successful transactions are `COMPLETED`; rejected ones are `REJECTED` with a reason. |
| BR-007 | *(Optional)* Amounts above the configured threshold (`APPROVAL_THRESHOLD = 10 000`) become `PENDING_APPROVAL`. |

---

## Intentional Legacy Characteristics

See [`docs/legacy-characteristics.md`](docs/legacy-characteristics.md) for the full list of
deliberate design flaws that make this a realistic modernisation target.

---

## Approved Transformation

See [`docs/transformation-target.md`](docs/transformation-target.md) for the bounded
modernisation task intended to be executed with IBM Bob.

---

## Scope Guardrail

This is **not** a production banking system. The purpose is to demonstrate the modernisation
workflow: analyse source, extract business rules, recommend a bounded transformation, execute
with IBM Bob, validate independently, and deliver the diff. Keep it small enough to repair
manually if a live demo fails.
