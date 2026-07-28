# AWS EC2 Deployment Guide — Distributed File Sharing Platform

Step-by-step instructions for deploying the full-stack platform (React Frontend + Spring Boot Backend + Infrastructure) onto a single AWS EC2 instance using Docker Compose.

---

## 📋 Architecture & Port Layout

The platform runs as containerized micro-services orchestrated by Docker Compose:

```
[ Web Browser ]
      │
      ├──(Port 80 HTTP)──► Nginx Container (Frontend SPA)
      │                         │
      │                  (Reverse Proxy /api/*)
      │                         │
      │                         ▼
      │                   Spring Boot App Container (Port 8080)
      │                         │
      │        ┌────────────────┼────────────────┐
      │        ▼                ▼                ▼
      │   PostgreSQL (Render) MinIO (S3)   Redis (Cache)   Kafka (Events)
```

### Why Nginx Reverse Proxy?
The Nginx frontend container serves static React assets and reverse-proxies `/api/*` requests directly to `http://app:8080` over the internal Docker network. This provides two key advantages:
1. **No Hardcoded Public IP**: The frontend build uses relative `/api/v1` endpoints, eliminating the need to bake the EC2 Elastic IP into JavaScript bundles at build time.
2. **Zero CORS Pre-flight Overhead**: Requests share the same origin (`http://<ec2-ip>:80`), avoiding cross-origin security restrictions.

---

## 🔒 Security Group Configuration (Inbound Rules)

| Type | Port Range | Source | Purpose |
| :--- | :--- | :--- | :--- |
| **SSH** | `22` | `My IP` | Secure SSH administration |
| **HTTP** | `80` | `0.0.0.0/0` | Public Web Frontend (React SPA) |
| **Custom TCP** | `8080` | `0.0.0.0/0` | Spring Boot API & Swagger UI |
| **Custom TCP** | `9001` | `My IP` | MinIO Web Console (Admin only) |

> [!NOTE]
> Internal ports `9000` (MinIO S3 API), `6379` (Redis), and `9092` (Kafka) are bound to `127.0.0.1` loopback inside `docker-compose.yml` to prevent public internet exposure.

---

## 🚀 Step-by-Step Deployment Procedure

### Step 1: Connect to EC2 Instance
```bash
ssh -i /path/to/your-key.pem ubuntu@<YOUR_ELASTIC_IP>
```

### Step 2: Run Automated Setup Script
```bash
curl -fsSL https://raw.githubusercontent.com/your-username/DisfileSys/main/deploy/setup-ec2.sh -o setup-ec2.sh
chmod +x setup-ec2.sh
./setup-ec2.sh https://github.com/your-username/DisfileSys.git
```

### Step 3: Configure Production `.env`
```bash
cd DisfileSys
cp .env.example .env
nano .env
```

Set real production values:
1. `POSTGRES_HOST`, `POSTGRES_USER`, `POSTGRES_PASSWORD` — Render database credentials.
2. `KAFKA_HOST` — Set to your EC2 Elastic IP.
3. `JWT_SECRET` — Generate a secure 256-bit key: `openssl rand -hex 32`.

### Step 4: Build & Launch Full Stack
```bash
docker compose up -d --build
```

### Step 5: Verify Deployment Health
Check running containers:
```bash
docker compose ps
```

Verify backend component health:
```bash
curl http://localhost:8080/actuator/health
```

Access the application in your browser:
- **Web App Frontend**: `http://<YOUR_ELASTIC_IP>` (Port 80)
- **API Swagger UI**: `http://<YOUR_ELASTIC_IP>:8080/swagger-ui.html`
