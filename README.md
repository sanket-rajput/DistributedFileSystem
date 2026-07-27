# Distributed File Sharing Platform — Production-Grade Backend

[![CI/CD Pipeline](https://img.shields.io/badge/CI%2FCD-passing-brightgreen)](.github/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue.svg)](Dockerfile)

Production-grade Spring Boot 3 backend for a Distributed File Sharing Platform. This project features multi-stage Docker packaging, containerized deployment readiness for AWS EC2, JWT authentication, RBAC, nested folder management, streaming MinIO S3 file storage, SHA-256 deduplication, file versioning, public link sharing, Kafka event-driven notifications, Redis metadata caching, JPA Specification search, cross-cutting AOP audit logging, Prometheus/Grafana observability, Testcontainers integration testing, and Kubernetes deployment manifests.

---

## 🛠 Tech Stack & Architecture

- **Java 21**, **Spring Boot 3.3.x**, **Spring Security**, **Spring Data JPA**, **Spring MVC**, **Spring AOP**, **Spring Cache**
- **PostgreSQL 16** (Metadata Store — Render external database)
- **MinIO** (S3-compatible Object Storage via official Java SDK v8.5.x)
- **Apache Kafka (KRaft mode)** (Asynchronous Event Streaming)
- **Redis 7** (Metadata Cache-Aside with 5-min TTL)
- **Spring Boot Actuator & Micrometer Prometheus** (Metrics & Health Indicators)
- **Grafana 10.4** (Monitoring Dashboards)
- **Testcontainers** (Isolated integration tests)
- **Docker Compose** & **Multi-Stage Dockerfile**
- **GitHub Actions CI/CD Pipeline**

---

## 📁 Package Structure (`package-by-feature`)

```
src/main/java/com/fileshare
├── DistributedFileSharingApplication.java
├── auth/            # JWT Token generation, authentication filter, Login & Register endpoints
├── user/            # User entity, Role (USER, ADMIN), UserRepository, User DTOs & service
├── folder/          # Folder entity (self-referencing parent), Folder CRUD & ownership rules
├── file/            # FileMetadata entity (SHA-256 hash), MinIO streaming upload/download, file CRUD
├── cache/           # Redis CacheManager configuration & 5-minute TTL cache-aside rules
├── event/           # Kafka producer configuration, topics, & event DTOs (FileUploaded, FileShared, FileDeleted)
├── notification/    # Kafka listener (@KafkaListener), notification persistence & paginated query endpoint
├── versioning/      # FileVersion entity, version listing, downloading, and version restoration
├── sharing/         # Share entity, public token generation, expiration/revocation, public unauthenticated endpoint
├── search/          # Dynamic combinable search filters using Spring Data JPA Specifications
├── audit/           # Cross-cutting AOP audit logging (@Auditable aspect) and ADMIN-only log inspection
├── common/          # GlobalExceptionHandler, standard ApiResponse<T>, custom exceptions
└── config/          # SecurityConfig, MinioConfig, SwaggerConfig, MetricsConfig
```

---

## 🐋 Containerized Local & Production Deployment

### 1. Environment Setup
Copy the environment configuration template and configure your parameters:

```bash
cp .env.example .env
```

Set external database credentials (Render Postgres), MinIO user/password, and generate a 256-bit JWT secret (`openssl rand -hex 32`).

### 2. Single-Command Launch via Docker Compose
Build the multi-stage Spring Boot application container and launch infrastructure services (MinIO, Redis, Kafka):

```bash
docker compose up -d --build
```

- **App API**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **MinIO Console**: `http://localhost:9001`

---

## 🏥 Actuator Health & Component Readiness Verification

Once containers are started, Spring Boot Actuator provides full component health inspection across all external infrastructure dependencies (PostgreSQL, MinIO, Redis, and Kafka):

```bash
curl -s http://localhost:8080/actuator/health | jq
```

### Expected Response:
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.15"
      }
    }
  }
}
```

A status of `UP` confirms that:
1. PostgreSQL metadata connection to Render DB is established.
2. MinIO S3 object storage bucket is accessible.
3. Redis in-memory cache server is accepting operations.
4. Kafka KRaft broker cluster is connected for asynchronous event messaging.

---

## ☁️ AWS EC2 Deployment Procedure

Instructions for deploying to a single AWS EC2 instance (`t3.medium`, Ubuntu 22.04 LTS):

1. **Provision EC2 & Security Group**:
   - Open ports `22` (SSH - restricted to your IP), `8080` (App API / Swagger), and `9001` (MinIO Console - restricted).
   - Internal ports `9000` (MinIO S3), `6379` (Redis), and `9092` (Kafka) are isolated to `127.0.0.1` inside `docker-compose.yml`.
2. **Execute First-Time Setup Script**:
   ```bash
   chmod +x deploy/setup-ec2.sh
   ./deploy/setup-ec2.sh <REPO_URL>
   ```
3. **Configure Production `.env`**:
   - Set `KAFKA_HOST=<YOUR_EC2_ELASTIC_IP>` and Render DB credentials.
4. **Deploy**:
   ```bash
   docker compose up -d --build
   ```

For detailed step-by-step documentation, see [deploy/README.md](file:///d:/3rd/ace/sem%207/project/DisfileSys/deploy/README.md).

---

## 🧪 Testing Strategy

### Unit Tests (Mockito)
Unit tests execute without external infrastructure dependencies:

```bash
mvn test
```

### Integration Tests (Testcontainers)
Integration tests spin up ephemeral Docker containers for PostgreSQL, Redis, Kafka, and MinIO:

```bash
mvn verify
```

Code coverage reports are generated automatically at `target/site/jacoco/index.html`.

---

## ⚙️ CI/CD Pipeline

The GitHub Actions workflow defined in [.github/workflows/ci.yml](file:///d:/3rd/ace/sem%207/project/DisfileSys/.github/workflows/ci.yml):
1. Sets up JDK 21 and Maven cache.
2. Executes `mvn verify` running unit and Testcontainers integration tests.
3. Verifies multi-stage `Dockerfile` compilation (`docker build .`).
4. Generates and uploads JaCoCo code coverage artifacts.

---

## 📊 Observability & Monitoring

### Launch Prometheus & Grafana
```bash
docker compose -f docker-compose.monitoring.yml up -d
```

- **Prometheus**: `http://localhost:9090` (scraping `/actuator/prometheus`)
- **Grafana**: `http://localhost:3000` (pre-configured dashboard with latency percentiles, throughput, and memory usage)

---

## ⚡ Target Performance Specifications

| Performance Metric Target | System Design / Architectural Specification | Mechanism |
| :--- | :--- | :--- |
| **5,000+ Files Managed** | Metadata indexing | PostgreSQL indexes (`sha256_hash`, `owner_id`, `folder_id`) |
| **300+ Concurrent Users** | Stateless scale | JWT Authentication + Spring Async Thread Pool |
| **10,000+ Kafka Events** | Event streaming | Non-blocking Kafka Producer (`KafkaTemplate.send()`) |
| **<150ms Metadata Lookup** | High-speed cache | Spring `@Cacheable` Redis Cache-Aside (<15ms cache hit) |
