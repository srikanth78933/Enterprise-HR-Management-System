# Enterprise HR Portal

A simple employee and department management web application built with **Java 21**, **Spring Boot 3.5**, **Thymeleaf**, **Bootstrap 5**,
**Spring Data JPA**, **PostgreSQL** and **Flyway**.

Everything is done from the browser: dashboard, employees (add / view / edit / delete / search / filter)
and departments (add / list / edit / delete).

## The four repositories

This repository is the application. It is built, deployed and monitored by three companion repositories:

| Repository | Purpose |
|------------|---------|
| **Enterprise-HR-Management-System** (this one) | Spring Boot application, tests |
| [enterprise-hr-portal-infra](https://github.com/srikanth78933/enterprise-hr-portal-infra) | Terraform: VPC, EKS, RDS, IRSA, add-ons, ELK server (dev / prod), Jenkins pipeline |
| [enterprise-hr-portal-deploy](https://github.com/srikanth78933/enterprise-hr-portal-deploy) | Jenkins CI/CD, Dockerfile, Helm chart: Maven → SonarQube → Nexus → Docker → Trivy → Docker Hub → EKS |
| [enterprise-hr-portal-monitoring](https://github.com/srikanth78933/enterprise-hr-portal-monitoring) | ELK on EC2, Filebeat + Metricbeat on EKS, Jenkins pipeline |

```
infra pipeline ──► AWS (VPC, EKS, RDS, ELK EC2)
deploy pipeline ─► builds this repo ─► Docker Hub ─► Helm ─► EKS pods ─► RDS
monitoring pipeline ─► ELK on EC2 ◄── Filebeat / Metricbeat on EKS ◄── app logs
```

## Features

| Area        | What you can do |
|-------------|-----------------|
| Dashboard   | Total employees, total departments, active / inactive employees, 5 most recently added employees |
| Employees   | List, add, view details, edit, delete (with confirmation), search by name or Employee ID, filter by department |
| Departments | List (with employee counts), add, edit, delete (only when the department has no employees) |
| Health      | `/actuator/health`, `/actuator/health/liveness`, `/actuator/health/readiness` |

Sample data (departments IT, HR, Finance, DevOps, Operations and 6 employees) is inserted by
Flyway the first time the application starts.

## Project structure

The Maven project sits at the root of the `Enterprise-HR-Management-System` repository.
The application is named `enterprise-hr-portal` (Maven artifact, JAR file and `spring.application.name`).

```
Enterprise-HR-Management-System/
├── pom.xml
├── mvnw, mvnw.cmd, .mvn/wrapper/          Maven wrapper (no Maven installation needed)
└── src/
    ├── main/
    │   ├── java/com/example/employee/
    │   │   ├── EmployeeApplication.java   Application entry point
    │   │   ├── config/                    DepartmentConverter, RequestLoggingFilter
    │   │   ├── controller/                MVC controllers + GlobalExceptionHandler
    │   │   ├── entity/                    Employee, Department, EmployeeStatus
    │   │   ├── exception/                 Simple application exceptions
    │   │   ├── repository/                Spring Data JPA repositories
    │   │   └── service/                   Business logic
    │   └── resources/
    │       ├── application.properties     All configuration (env-variable driven)
    │       ├── db/migration/              Flyway SQL scripts (V1 schema, V2 sample data)
    │       ├── static/css, static/js      Theme stylesheet, delete confirmation script
    │       └── templates/                 Thymeleaf pages + fragments/layout.html
    └── test/java/com/example/employee/   JUnit 5 + Mockito tests (services, request logging)
```

## Configuration

All settings are read from environment variables, so the same JAR works with local PostgreSQL
and with AWS RDS PostgreSQL.

| Variable      | Default          | Description |
|---------------|------------------|-------------|
| `DB_HOST`     | `localhost`      | PostgreSQL host (RDS endpoint in AWS) |
| `DB_PORT`     | `5432`           | PostgreSQL port |
| `DB_NAME`     | `employee_db`    | Database name |
| `DB_USERNAME` | `employee_user`  | Database user |
| `DB_PASSWORD` | *(none)*         | Database password — **required**, never hardcoded |
| `DB_SSL_MODE` | `prefer`         | PostgreSQL SSL mode (`prefer` works locally and with RDS; use `require` to force SSL) |
| `SERVER_PORT` | `8080`           | HTTP port of the application |

The database schema is created by Flyway (`src/main/resources/db/migration`). Hibernate only
*validates* that the entities match the schema (`spring.jpa.hibernate.ddl-auto=validate`).

## Setup on Ubuntu

### 1. Install prerequisites

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk postgresql postgresql-contrib curl
java -version
sudo systemctl enable --now postgresql
```

Maven itself does not need to be installed; the Maven wrapper (`./mvnw`) downloads it.

### 2. Create the PostgreSQL database and user

```bash
sudo -u postgres psql <<'SQL'
CREATE USER employee_user WITH PASSWORD 'ChangeMe123!';
CREATE DATABASE employee_db OWNER employee_user;
GRANT ALL PRIVILEGES ON DATABASE employee_db TO employee_user;
SQL
```

Check the connection:

```bash
PGPASSWORD='ChangeMe123!' psql -h localhost -U employee_user -d employee_db -c 'SELECT version();'
```

### 3. Configure the database environment variables

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=employee_db
export DB_USERNAME=employee_user
export DB_PASSWORD='ChangeMe123!'
```

These only last for the current terminal. Run them again in every new terminal (or put them in a
file such as `~/employee-db.env` and run `source ~/employee-db.env`).

### 4. Run the application with Maven

```bash
cd Enterprise-HR-Management-System
chmod +x mvnw
./mvnw spring-boot:run
```

Wait for `Started EmployeeApplication` in the log. Flyway creates the tables and sample data on
the first start.

### 5. Open the application in a browser

```
http://localhost:8080
```

Health check:

```bash
curl http://localhost:8080/actuator/health
# {"status":"UP","groups":["liveness","readiness"]}
```

### 6. Build the application JAR

```bash
./mvnw clean package
java -jar target/enterprise-hr-portal.jar
```

The JAR uses the same environment variables. To use another port:
`SERVER_PORT=9090 java -jar target/enterprise-hr-portal.jar`.

### 7. Run the tests

```bash
./mvnw test
```

The unit tests mock the repositories, so they do not need a database.

**Coverage:** JaCoCo runs with the tests. `./mvnw package` (or `./mvnw test`) writes
`target/site/jacoco/index.html` (open it in a browser) and `target/site/jacoco/jacoco.xml`,
which SonarQube reads automatically. The SonarQube quality gate requires 80% coverage on new code,
so add tests together with new code.

## Logging

The application logs to the console (stdout), which Kubernetes collects and Filebeat ships to ELK.

| Logger | Level | Example |
|--------|-------|---------|
| `RequestLoggingFilter` | INFO | `GET /employees -> 200 (12 ms)` (one line per page; health checks and static files skipped) |
| `EmployeeService` | INFO | `Created employee EMP007 (id=7, department=IT)`, `Updated …`, `Deleted …` |
| `DepartmentService` | INFO / WARN | `Created department Legal (id=6)`; WARN `Blocked delete of department IT (id=1): it has 1 employee(s)` |
| `GlobalExceptionHandler` | WARN | `Resource not found: Employee not found with ID 99` |

Log levels can be changed without code changes through environment variables, for example
`LOGGING_LEVEL_COM_EXAMPLE_EMPLOYEE=DEBUG` (the deploy repo sets this per environment).

## Pages and URLs

| Page              | URL |
|-------------------|-----|
| Dashboard         | `/` |
| Employee list     | `/employees` (optional `?keyword=...&departmentId=...`) |
| Add employee      | `/employees/new` |
| Employee details  | `/employees/{id}` |
| Edit employee     | `/employees/{id}/edit` |
| Department list   | `/departments` |
| Add department    | `/departments/new` |
| Edit department   | `/departments/{id}/edit` |

Create, update and delete are standard HTML form `POST`s to the same URLs
(`/employees`, `/employees/{id}`, `/employees/{id}/delete`, and the same for `/departments`).

## Running on AWS (EKS + RDS)

The deploy pipeline runs the same JAR in a container on EKS. No code changes are needed: the Helm chart
sets the same environment variables, with `DB_USERNAME` / `DB_PASSWORD` read from AWS Secrets Manager.
To point a local run at RDS instead:

```bash
export DB_HOST=mydb.xxxxxxxx.ap-south-1.rds.amazonaws.com
export DB_PORT=5432
export DB_NAME=employee_db
export DB_USERNAME=employee_user
export DB_PASSWORD='<from AWS Secrets Manager / Kubernetes Secret>'
export DB_SSL_MODE=require
```

For Kubernetes probes use `/actuator/health/liveness` (liveness) and
`/actuator/health/readiness` (readiness, which also checks the database).

## Troubleshooting

| Problem | Fix |
|---------|-----|
| `Could not resolve placeholder 'DB_PASSWORD'` | `DB_PASSWORD` is not set in the current terminal. Export it (step 3). |
| `password authentication failed for user` | The username/password do not match the PostgreSQL user created in step 2. |
| `Port 8080 was already in use` | Stop the other process, or run with `SERVER_PORT=8081`. |
| `invalid value for parameter "TimeZone": "Asia/Calcutta"` (mostly on Windows) | The JVM uses an old time-zone name. Run with `JAVA_TOOL_OPTIONS=-Duser.timezone=Asia/Kolkata` (or `UTC`). |
| Page has no styling | Bootstrap is loaded from the jsDelivr CDN, so the browser needs internet access. |
