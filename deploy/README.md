# AWS EC2 Deployment Guide — Distributed File Sharing Platform

Step-by-step instructions for deploying the platform onto a single AWS EC2 instance using Docker Compose.

---

## 📋 Prerequisites & EC2 Launch Configuration

### 1. Launch EC2 Instance
- **AMI**: Ubuntu Server 22.04 LTS (HVM), SSD Volume Type.
- **Instance Type**: `t3.medium` (2 vCPU, 4 GiB Memory minimum recommended to run Java 21, MinIO, Redis, and Kafka KRaft).
- **Storage**: 20 GiB GP3 EBS root volume.
- **Elastic IP**: Allocate an Elastic IP in AWS Console and associate it with your EC2 instance.

### 2. Security Group Configuration (Inbound Rules)

| Type | Port Range | Source | Purpose |
| :--- | :--- | :--- | :--- |
| **SSH** | `22` | `My IP` | Secure SSH administration |
| **Custom TCP** | `8080` | `0.0.0.0/0` | Public API & Swagger UI access |
| **Custom TCP** | `9001` | `My IP` | MinIO Web Console (Admin only) |

> [!NOTE]
> Ports `9000` (MinIO S3 API), `6379` (Redis), and `9092` (Kafka) are bound to `127.0.0.1` inside `docker-compose.yml` to prevent public internet access. They communicate securely over the internal Docker network.

---

## 🚀 Step-by-Step Deployment Guide

### Step 1: Connect to EC2 Instance
```bash
ssh -i /path/to/your-key.pem ubuntu@<YOUR_ELASTIC_IP>
```

### Step 2: Run Automated Setup Script
Clone repository and install Docker, Docker Compose, and dependencies:

```bash
curl -fsSL https://raw.githubusercontent.com/your-username/DisfileSys/main/deploy/setup-ec2.sh -o setup-ec2.sh
chmod +x setup-ec2.sh
./setup-ec2.sh https://github.com/your-username/DisfileSys.git
```

### Step 3: Configure Environment Variables
Navigate into project directory and copy `.env.example`:

```bash
cd DisfileSys
cp .env.example .env
nano .env
```

Set real production values:
1. `POSTGRES_HOST`, `POSTGRES_USER`, `POSTGRES_PASSWORD` — Render database credentials.
2. `KAFKA_HOST` — Set to your EC2 Elastic IP.
3. `JWT_SECRET` — Generate a secure 256-bit key:
   ```bash
   openssl rand -hex 32
   ```

### Step 4: Build & Launch Containers
```bash
docker compose up -d --build
```

### Step 5: Verify Deployment Health
Check status of running containers:
```bash
docker compose ps
```

Verify application health hitting Spring Actuator:
```bash
curl http://localhost:8080/actuator/health
```

Expected JSON response:
```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "diskSpace": { "status": "UP" },
    "ping": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

Open Swagger UI in your browser:
👉 **`http://<YOUR_ELASTIC_IP>:8080/swagger-ui.html`**
