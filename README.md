# ipos-ca

pharmacy counter assistant for cosymed ltd, built by cortexx (team b).

swing desktop app with mysql backend. handles sales, stock, customers, orders, reports, and user management.

## what you need

- java 17+ (we use adoptium temurin)
- mysql 8.0+
- maven 3.9+ (intellij bundles one, or install separately)

## setup

### 1. database

run the sql scripts in order against your mysql server:

```
mysql -u root -p < db/01_schema.sql
mysql -u root -p iposca_database < db/02_reference_data.sql
mysql -u root -p iposca_database < db/03_demo_data.sql
```

this creates the `iposca_database` schema with all tables and seed data.

### 2. config

```
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

open `db.properties` and set your mysql password. the file is gitignored so your credentials stay local.

### 3. build and run

```
mvn clean compile exec:java -Dexec.mainClass="ie.cortexx.Main"
```

or just run `ie.cortexx.Main` from intellij (right-click > run).

### 4. login

the seed data creates one admin account: `sysdba`. you can create additional users (pharmacist, cashier, manager) from the user management panel after logging in.

## tests

unit tests:
```
mvn test
```

integration tests (needs a running mysql with the test schema):
```
mvn verify
```

coverage report ends up at `target/site/jacoco/index.html`.

## project layout

```
src/main/java/ie/cortexx/
  model/       entities and enums
  dao/         database access
  service/     business logic
  gui/         swing ui (sub-packaged by feature)
  util/        db connection, session manager, helpers
  interfaces/  sa and pu integration contracts
  impl/        contract implementations
db/            sql schema + seed data
```

## notes

- the app uses flatlaf for theming. dark, light, green, and blue themes are available from the palette icon in the sidebar.
- font family and size can also be changed from the appearance dialog.
- pdf report export works out of the box with the maven build, no extra tools needed.
- sa integration expects team 46's database on `db.sa.url` (see db.properties).


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

