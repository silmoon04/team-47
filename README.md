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
