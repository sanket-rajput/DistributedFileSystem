# Distributed File Sharing Platform — Full Stack System

[![CI/CD Pipeline](https://img.shields.io/badge/CI%2FCD-passing-brightgreen)](.github/workflows/ci.yml)
[![Java 21](https://img.shields.io/badge/Java-21-blue.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18-blue.svg)](frontend/)
[![Docker](https://img.shields.io/badge/Docker-Compose--Ready-blue.svg)](docker-compose.yml)

Production-grade Distributed File Sharing Platform featuring a Spring Boot 3 backend and a React 18 + Vite + Tailwind CSS frontend packaged for containerized deployment via Docker Compose.

---

## 🛠 Architecture Overview

```
[ Web Browser ] ──(Port 80)──► Nginx Container (React SPA + /api/* Reverse Proxy)
                                      │
                                      ▼
                        Spring Boot App Container (Port 8080)
                                      │
                 ┌────────────────────┼────────────────────┐
                 ▼                    ▼                    ▼
          PostgreSQL (Render)    MinIO (S3)           Redis (Cache)
```

### Key Architectural Highlights
- **Nginx Reverse Proxy**: Frontend static assets are served via Nginx on port 80. Nginx proxies `/api/*` requests directly to `http://app:8080` over the internal Docker network, avoiding hardcoded public IP addresses in JavaScript assets and eliminating cross-origin preflight overhead.
- **Backend Metadata & Storage**: PostgreSQL metadata store (Render), MinIO S3 object storage streaming uploads, Redis metadata cache-aside (<15ms target), and Apache Kafka (KRaft mode) event streaming.
- **Frontend Design System**: Google/Gemini 4-color accent palette (`#4285F4`, `#EA4335`, `#FBBC05`, `#34A853`) on warm neutral base canvas (`#FFFFFF` & `#F5E6D8`).

---

## 🚀 Running the Application

### Option A: Full Container Stack via Docker Compose (Recommended)

1. Copy `.env.example` to `.env` and fill in your parameters:
   ```bash
   cp .env.example .env
   ```
2. Build and launch all containers (Frontend, Backend, MinIO, Redis, Kafka):
   ```bash
   docker compose up -d --build
   ```
3. Access Web App:
   - **Frontend App**: `http://localhost` (or `http://<EC2_PUBLIC_IP>`)
   - **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### Option B: Local Frontend Development (`npm run dev`)

1. Start backend dependencies:
   ```bash
   docker compose up -d minio redis kafka
   mvn spring-boot:run
   ```
2. Start frontend dev server:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```
   Frontend dev server runs on `http://localhost:3000` with direct API connection to `http://localhost:8080`.

---

## 🔒 Security Note on CORS
`SecurityConfig.java` defines an explicit `CorsConfigurationSource` (`setAllowedOriginPatterns(List.of("*"))`). When deployed behind the Nginx reverse proxy on the same origin (`port 80`), browser CORS preflight checks are bypassed. The permissive CORS configuration remains enabled to support local development (`localhost:3000`) and direct API clients.

---

## 📑 Detailed Guides
- **AWS EC2 Deployment Guide**: See [deploy/README.md](file:///d:/3rd/ace/sem%207/project/DisfileSys/deploy/README.md) for step-by-step setup instructions.
- **CI/CD Pipeline**: GitHub Actions workflow in [.github/workflows/ci.yml](file:///d:/3rd/ace/sem%207/project/DisfileSys/.github/workflows/ci.yml) builds unit tests, Testcontainers integration tests, and Docker images.
