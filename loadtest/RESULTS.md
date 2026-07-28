# Production Benchmark Results

> [!IMPORTANT]
> **INSTRUCTION FOR BENCHMARK EXECUTOR:**
> Do NOT fabricate or invent benchmark numbers in this document. 
> Execute the k6 load test suite against the live production environment (`https://fileshare.sanketrajput.live`), copy the actual metric output from the k6 terminal summary report, and populate the placeholders below.

---

## 📅 Benchmark Execution Details

- **Execution Date**: `[Pending Benchmark Run]`
- **Target Host**: `https://fileshare.sanketrajput.live`
- **Environment**: AWS EC2 (t3.medium, 2 vCPU, 4GB RAM)
- **Peak Concurrent Users (VUs)**: `[To be populated after benchmark run]`
- **Total Benchmark Duration**: `[To be populated after benchmark run]`

---

## 🚀 1. Authentication & CRUD Load Benchmark (`auth-and-crud-load.js`)

| Operation | p50 (Median) | p90 | p95 | p99 | Average | Max |
|---|---|---|---|---|---|---|
| **User Login** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |
| **Multipart Upload** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |
| **File List Retrieval** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |
| **Cache Miss GET** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |
| **Cache Hit GET** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |
| **File Delete** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |

### Additional Operational Metrics
- **Iteration Duration**: `[Pending]` ms (p95: `[Pending]` ms)
- **Upload Throughput**: `[Pending]` MB/s

---

## 🔍 2. Search Endpoint Benchmark (`search-benchmark.js`)

*Prerequisite: Run after executing `node loadtest/seed-5000-files.js`*

| Search Filter Variation | p50 (Median) | p95 | p99 | Average | Max |
|---|---|---|---|---|---|
| **Overall Search Queries** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |
| **Default Pagination (`?page=0&size=20`)** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |
| **Name Filter (`?name=seed_file`)** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |
| **Content-Type Filter (`?contentType=text/plain`)** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |
| **Size Range Filter (`?minSize=10&maxSize=10000`)** | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms | `[Pending]` ms |

---

## ⚡ 3. Kafka Event Burst Benchmark (`kafka-event-burst.js`)

- **Total HTTP Requests Executed**: `[To be populated]`
- **Estimated Kafka Events Produced**: `[To be populated]` (Upload + Share events)
- **Benchmark Duration**: `[To be populated]`
- **HTTP Request Throughput**: `[To be populated]` req/sec
- **Kafka Event Rate**: `[To be populated]` events/sec
- **Upload Latency**: p50: `[Pending]` ms | p95: `[Pending]` ms | p99: `[Pending]` ms
- **Share Latency**: p50: `[Pending]` ms | p95: `[Pending]` ms | p99: `[Pending]` ms
- **Consumer Processing Verification**:
  - Prometheus Metric (`kafka_events_published_total`): `[Verify via Grafana/Prometheus]`
  - Consumer Group Lag: `[Verify via Kafka metrics]`
  - Database Notification Rows Created: `[Verify in PostgreSQL notifications table]`

---

## 📊 4. System Reliability & Infrastructure Resource Utilization

| Metric Category | Measured Value | Threshold Limit / Target |
|---|---|---|
| **Request Failure Rate (HTTP Error %)** | `[Pending]` % | `< 5.0 %` |
| **Peak Concurrent Users (VUs)** | `[Pending]` VUs | `300 VUs` |
| **AWS EC2 CPU Utilization** | `[Pending]` % | `< 85 %` |
| **AWS EC2 Memory Utilization** | `[Pending]` % | `< 80 %` |
| **Disk Storage Utilization** | `[Pending]` % / GB | Monitor MinIO volume |
| **Network Throughput (In/Out)** | `[Pending]` Mbps | — |
