# AWS EC2 Deployment & HTTPS Guide — Distributed File Sharing Platform

Production setup guide for `fileshare.sanketrajput.live` with automated Let's Encrypt SSL certificates (Certbot), Nginx reverse proxy, and Docker Compose orchestration.

---

## 📋 Production Architecture & Endpoints

```
[ Web Browser ]
      │
      ├──(Port 80 HTTP Redirect / ACME)──► Nginx Container (Port 80)
      │                                         │ (301 Redirect to HTTPS)
      ├──(Port 443 HTTPS SSL)───────────► Nginx Container (Port 443)
      │                                         │
      │                                  (Reverse Proxy /api/*)
      │                                         │
      │                                         ▼
      │                                Spring Boot App Container (Port 8080)
      │                                         │
      │                  ┌──────────────────────┼──────────────────────┐
      │                  ▼                      ▼                      ▼
      │             PostgreSQL (Render)    MinIO (S3 Storage)    Redis (Cache)
```

- **Production Domain**: `https://fileshare.sanketrajput.live`
- **HTTP Auto-Redirect**: All plain HTTP requests on port 80 automatically 301-redirect to `https://fileshare.sanketrajput.live`.

---

## 🔒 Security Group Configuration (Inbound Rules)

Ensure the following inbound ports are open in AWS EC2 Security Group:

| Type | Port Range | Source | Purpose |
| :--- | :--- | :--- | :--- |
| **SSH** | `22` | `My IP` + GitHub Actions Runner `/32` | Secure SSH administration and CI/CD deployment |
| **HTTP** | `80` | `0.0.0.0/0` | ACME Challenge Verification & HTTP -> HTTPS Redirect |
| **HTTPS** | `443` | `0.0.0.0/0` | Secure Web Frontend (`https://fileshare.sanketrajput.live`) |
| **Custom TCP** | `8080` | `0.0.0.0/0` | Direct Spring Boot API & Swagger UI |
| **Custom TCP** | `9001` | `My IP` | MinIO Web Console (Admin access only) |

---

### CI/CD SSH Access Note
The deployment workflow (`.github/workflows/deploy.yml`) temporarily whitelists the current GitHub Actions runner public IP on port `22` before deployment and revokes it afterward.

Required repository secrets for this automation:
- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`

Required repository variables (or secrets) for this automation:
- `AWS_REGION`
- `EC2_SECURITY_GROUP_ID`

If these are configured as **environment-level** variables/secrets, set them in the `production` environment (the deploy job targets `environment: production`).

## 🔑 One-Time SSL Certificate Issuance (Let's Encrypt)

Before issuing the certificate, ensure DNS A-Record for **`fileshare.sanketrajput.live`** points directly to your EC2 Elastic IP address.

### Step 1: Launch Nginx Service (HTTP Port 80)
```bash
docker compose up -d frontend app minio redis kafka
```

### Step 2: Issue Certificate via Certbot Webroot
Run the initial certificate request command:
```bash
docker compose run --rm certbot certonly \
  --webroot \
  -w /var/www/certbot \
  -d fileshare.sanketrajput.live \
  --email sanket@example.com \
  --agree-tos \
  --no-eff-email
```

### Step 3: Reload Nginx to Activate SSL (Port 443)
Once the certificate files are created under `/etc/letsencrypt/live/fileshare.sanketrajput.live/`:
```bash
docker compose exec frontend nginx -s reload
```

---

## 🔄 Automatic SSL Certificate Renewal

The `certbot` container service defined in `docker-compose.yml` automatically checks for certificate renewal every 12 hours:

```yaml
certbot:
  image: certbot/certbot:latest
  container_name: fileshare-certbot
  restart: always
  volumes:
    - certbot_www:/var/www/certbot
    - certbot_certs:/etc/letsencrypt
  entrypoint: "/bin/sh -c 'trap exit TERM; while :; do certbot renew; sleep 12h & wait $${!}; done;'"
```

To manually test certificate renewal at any time:
```bash
docker compose run --rm certbot renew --dry-run
```

---

## 🚀 Full Stack Launch & Verification

```bash
# 1. Start all container services
docker compose up -d --build

# 2. Check running services
docker compose ps

# 3. Access Application
# Web Frontend: https://fileshare.sanketrajput.live
# API Documentation: https://fileshare.sanketrajput.live/swagger-ui.html (or http://<EC2-IP>:8080/swagger-ui.html)
```
