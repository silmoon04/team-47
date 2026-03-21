# IPOS-CA — Pharmacy Counter Assistant

> CortexX (Team B) | Software Engineering | Demo: 16 April 2026

## Quick Start

### Prerequisites
- JDK 17+ (Adoptium Temurin)
- MySQL 8.0+
- Maven 3.9+ (bundled with IntelliJ)

### Database Setup
```bash
mysql -u root -p < db/01_schema.sql
mysql -u ipos_app -p ipos_ca < db/02_reference_data.sql
mysql -u ipos_app -p ipos_ca < db/03_demo_data.sql
```

### Configure
```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
# Edit db.properties with your MySQL credentials
```

### Build & Run
```bash
mvn clean compile exec:java -Dexec.mainClass="ie.cortexx.Main"
```

### Run Tests
```bash
mvn test
# Coverage report: target/site/jacoco/index.html
```

## Project Structure
```
src/main/java/ie/cortexx/
 - model/         Entity classes + enums
 -  dao/           Data access objects
 -  service/       Business logic
 -  impl/          Interface implementations (CAtoPU)
 -  interfaces/    I_SAtoCA, I_CAtoPU contracts
 -  exception/     Custom exceptions
 -  gui/           Swing panels (sub-packaged by feature)
- util/          DBConnection, SessionManager
```


## Test Workflow Setup (H1)

This project uses Maven, JUnit 5, JaCoCo, and Failsafe to provide a repeatable test workflow for all team members.

---

### Prerequisites

Ensure the following are installed:

* Java 17+
* Maven 3.9+
* MySQL Server 8.4.x (running locally)

---

### Database Setup (Required for Integration Tests)

Start MySQL and run the following:

```sql
CREATE DATABASE IF NOT EXISTS demo_db;

CREATE USER IF NOT EXISTS 'demo_user'@'localhost' IDENTIFIED BY 'demo_pass';
ALTER USER 'demo_user'@'localhost' IDENTIFIED BY 'demo_pass';

GRANT ALL PRIVILEGES ON demo_db.* TO 'demo_user'@'localhost';

FLUSH PRIVILEGES;
```

Then create the required table:

```sql
USE demo_db;

CREATE TABLE IF NOT EXISTS demo_table (
    id INT AUTO_INCREMENT PRIMARY KEY
);

TRUNCATE TABLE demo_table;
```

---

### Test Commands

Run the following from the project root:

```bash
mvn clean test
mvn jacoco:report
mvn clean verify
```

---

### Output Locations

* Unit test reports:
  `target/surefire-reports/`

* Integration test reports:
  `target/failsafe-reports/`

* Coverage report:
  `target/site/jacoco/index.html`

Open coverage report with:

```bash
open target/site/jacoco/index.html
```

---

### Test Structure

* Unit tests: `*Test.java` → run via Surefire
* Integration tests: `*IT.java` → run via Failsafe

Example:

* `AppTest.java` → unit test
* `DBTestIT.java` → integration test

---

### Verification

* All tests pass using `mvn clean verify`
* Coverage report is generated successfully
* Integration tests require the database setup above
* If DB is misconfigured, Maven will fail with a non-zero exit status

---

### Notes

* Integration tests use:

  * Database: `demo_db`
  * User: `demo_user`
  * Password: `demo_pass`
* Ensure MySQL is running before executing tests

