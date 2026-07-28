# Production Load Testing & Benchmark Suite

Comprehensive k6 and Node.js performance testing suite built for the **DistributedFileSystem** live production environment deployed at **`https://fileshare.sanketrajput.live`**.

---

## ⚠️ Live Production Infrastructure Warnings

The live target environment operates on an **AWS EC2 (t3.medium)** instance running Spring Boot, PostgreSQL, Redis, Kafka, and MinIO inside Docker containers.

> [!WARNING]
> **EC2 Resource Limits**: Ramping directly to 300 concurrent Virtual Users (VUs) without warmup can cause CPU throttling and Nginx 502/504 Gateway errors.
> 
> **PostgreSQL Connection Pool**: High concurrent requests can exhaust HikariCP DB connection limits if connection pool size is exceeded.
> 
> **Kafka Queue Growth**: The Kafka event burst test generates thousands of events in minutes. Monitor Kafka topic lag to ensure consumers catch up.
> 
> **Disk Usage**: Running the 5,000 file seed script will consume PostgreSQL metadata records and MinIO storage on EC2. Execute `node loadtest/cleanup-clean-files.js` after benchmarking to restore space.

---

## 📈 Recommended Execution Progression

To avoid overwhelming EC2 resources, **DO NOT start with 300 VUs**. Run benchmarks in stages and monitor failure rates:

```
10 VUs  ──►  25 VUs  ──►  50 VUs  ──►  100 VUs  ──►  200 VUs  ──►  300 VUs
```

> [!CAUTION]
> **Abort Criteria**: Abort progression immediately if any of the following occur:
> - HTTP request failure rate exceeds **5%**
> - JVM memory exhaustion or OutOfMemoryErrors occur
> - PostgreSQL connection pool saturation (HikariCP pool timeouts)
> - Kafka consumer backlog grows excessively

---

## 🛠️ Prerequisites & Installation

### 1. Install k6
- **Windows (Chocolatey)**: `choco install k6`
- **Windows (Winget)**: `winget install k6`
- **macOS (Homebrew)**: `brew install k6`
- **Linux (Debian/Ubuntu)**:
  ```bash
  sudo gpg -k
  sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5BB7B4F1373C4501E8161D951C22A7C1BF825C9
  echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
  sudo apt-get update
  sudo apt-get install k6
  ```

### 2. Node.js Environment
Node.js v18.0.0 or higher (for `seed-5000-files.js` and `cleanup-clean-files.js`).

---

## 🧪 Benchmark Suite Commands

### 1. Authentication & CRUD Load Benchmark (`auth-and-crud-load.js`)
Stresses authentication, multipart upload, file listing, cold/warm Redis metadata caching, and file deletion.

- **Uses native `response.timings.duration`** for exact sub-millisecond precision.
- **Execution Commands**:
  ```bash
  # Step 1: 10 VUs Warmup
  k6 run -e TARGET_VUS=10 loadtest/auth-and-crud-load.js

  # Step 2: 50 VUs Stage
  k6 run -e TARGET_VUS=50 loadtest/auth-and-crud-load.js

  # Step 3: 100 VUs Stage
  k6 run -e TARGET_VUS=100 loadtest/auth-and-crud-load.js

  # Step 4: Full 300 VUs Benchmark
  k6 run -e TARGET_VUS=300 loadtest/auth-and-crud-load.js
  ```

---

### 2. Database Seeding Tool (`seed-5000-files.js`)
Populates 5,000 file records to enable realistic search and pagination benchmarks.

- **Features**: Resumable execution state saved in `loadtest/.seed-progress.json`.
- **Execution Command**:
  ```bash
  node loadtest/seed-5000-files.js
  ```

---

### 3. Search Endpoint Benchmark (`search-benchmark.js`)
Stresses `GET /api/v1/files/search` with combinable JPA specification query filters (pagination, name, contentType, min/max size).

- **Execution Command**:
  ```bash
  k6 run loadtest/search-benchmark.js
  ```

---

### 4. Kafka Event Burst Benchmark (`kafka-event-burst.js`)
Stresses Kafka producer endpoints (`/upload` and `/share` with `{ "permission": "DOWNLOAD" }`) to generate **10,000+ events**.

- **Verification Note**: Consumer throughput must be independently verified using Prometheus, Grafana, and database notifications.
- **Execution Command**:
  ```bash
  k6 run loadtest/kafka-event-burst.js
  ```

---

### 5. Production Artifact Cleanup Tool (`cleanup-clean-files.js`)
Purges all test-generated files (`seed_file_*`, `loadtest_*`, `event_*`) from production PostgreSQL and MinIO.

- **Execution Command**:
  ```bash
  node loadtest/cleanup-clean-files.js
  ```

---

## 📊 Recording Benchmark Results

After executing your tests against `https://fileshare.sanketrajput.live`, copy the exact metric summary output from k6 into [loadtest/RESULTS.md](file:///d:/3rd/ace/sem%207/project/DisfileSys/loadtest/RESULTS.md) and update the main [README.md](file:///d:/3rd/ace/sem%207/project/DisfileSys/README.md).
