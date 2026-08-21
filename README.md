# Distributed File Sharing Platform : Full Stack System

[![CI/CD Pipeline](https://img.shields.io/badge/CI%2FCD-passing-brightgreen)](.github/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](frontend/)
[![HTTPS](https://img.shields.io/badge/HTTPS-Let's%20Encrypt-brightgreen.svg)](https://fileshare.sanketrajput.live)

Production-grade Distributed File Sharing Platform featuring a Spring Boot 3 backend and a React 18 + Vite + Tailwind CSS frontend deployed with Let's Encrypt SSL/TLS on **`https://fileshare.sanketrajput.live`**.

---

## 🛠 Architecture Overview

```
[ Web Browser ]
      │
      ├──(Port 80 HTTP)────► Nginx (301 Redirect to HTTPS / ACME Challenge)
      └──(Port 443 HTTPS)──► Nginx Container (React SPA + /api/* Reverse Proxy)
                                    │
                                    ▼
                      Spring Boot App Container (Port 8080)
                                    │
               ┌────────────────────┼────────────────────┐
               ▼                    ▼                    ▼
        PostgreSQL (Render)    MinIO (S3)           Redis (Cache)
```

### Key Architectural Highlights
- **Production Domain & HTTPS**: Deployed at `https://fileshare.sanketrajput.live` with automated Let's Encrypt SSL certificate issuance and auto-renewal via Certbot.
- **Nginx Reverse Proxy**: Serves React SPA assets over HTTPS port 443 and reverse-proxies `/api/*` requests to `http://app:8080` over the internal Docker network.
- **CORS Allow-List**: Strict production CORS configuration in `SecurityConfig.java` restricting origins to `https://fileshare.sanketrajput.live` (no wildcards).

---

## 🚀 Running the Full Stack

### Launch Container Stack
```bash
cp .env.example .env
docker compose up -d --build
```

Access endpoints:
- **Web App**: `https://fileshare.sanketrajput.live`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`

---

## 🔒 Security Note on CORS
`SecurityConfig.java` restricts cross-origin requests exclusively to:
- `https://fileshare.sanketrajput.live`
- `http://fileshare.sanketrajput.live`
- `http://localhost:3000` / `http://localhost:5173` (Local dev)

---

## ⚡ Performance & Scale Validation

Production benchmarks are defined in [/loadtest](file:///d:/3rd/ace/sem%207/project/DisfileSys/loadtest) using k6 and Node.js targeting `https://fileshare.sanketrajput.live`.

### Measured Benchmarks (AWS EC2 t3.medium)
*(Metrics will be populated from actual k6 outputs after benchmark execution)*

| Metric Category | p50 (Median) | p95 | p99 | Target / Threshold |
|---|---|---|---|---|
| **User Authentication / Login** | `[Pending]` | `[Pending]` | `[Pending]` | `< 1500 ms` |
| **Multipart File Upload** | `[Pending]` | `[Pending]` | `[Pending]` | `< 3000 ms` |
| **File List Retrieval** | `[Pending]` | `[Pending]` | `[Pending]` | `< 1000 ms` |
| **File Search (JPA Spec)** | `[Pending]` | `[Pending]` | `[Pending]` | `< 1000 ms` |
| **Metadata GET (Cache Miss)** | `[Pending]` | `[Pending]` | `[Pending]` | DB Query + Redis Warmup |
| **Metadata GET (Cache Hit)** | `[Pending]` | `[Pending]` | `[Pending]` | Direct Redis Cache (< 300 ms) |
| **File Deletion** | `[Pending]` | `[Pending]` | `[Pending]` | Cleanup per Iteration |
| **Kafka Event Burst** | `[Pending] events/s` | `[Pending]` | `[Pending]` | Target 10,000+ Burst Events |
| **Peak Concurrent Users** | `[Pending] VUs` | — | — | Target 300 VUs |
| **Request Failure Rate** | `[Pending] %` | — | — | `< 5.0 %` |

For full load test scripts, instructions, and results logging:
- See [loadtest/README.md](file:///d:/3rd/ace/sem%207/project/DisfileSys/loadtest/README.md) for running benchmarks (`k6 run loadtest/auth-and-crud-load.js`).
- See [loadtest/RESULTS.md](file:///d:/3rd/ace/sem%207/project/DisfileSys/loadtest/RESULTS.md) for raw metric output templates.

---

## 📑 Detailed Guides
- **Load Testing Suite**: See [loadtest/README.md](file:///d:/3rd/ace/sem%207/project/DisfileSys/loadtest/README.md) for k6 & seeding scripts.
- **AWS EC2 & HTTPS Guide**: See [deploy/README.md](file:///d:/3rd/ace/sem%207/project/DisfileSys/deploy/README.md) for step-by-step SSL issuance and EC2 setup.
- **CI/CD Pipeline**: GitHub Actions workflow in [.github/workflows/ci.yml](file:///d:/3rd/ace/sem%207/project/DisfileSys/.github/workflows/ci.yml).


