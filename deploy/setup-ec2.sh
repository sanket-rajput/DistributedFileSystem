#!/usr/bin/env bash
# ==============================================================================
# First-Time EC2 Instance Setup Script (Ubuntu 22.04 LTS)
# Installs Docker, Docker Compose, Git, and clones the repository.
# Usage: ./setup-ec2.sh [REPO_URL]
# ==============================================================================

set -euo pipefail

REPO_URL="${1:-https://github.com/your-username/DisfileSys.git}"
TARGET_DIR="DisfileSys"

echo "=== 1. Updating System Packages ==="
sudo apt-get update -y
sudo apt-get upgrade -y
sudo apt-get install -y ca-certificates curl gnupg lsb-release git

echo "=== 2. Installing Official Docker Engine & Docker Compose Plugin ==="
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg --yes

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "=== 3. Adding 'ubuntu' User to Docker Group ==="
sudo usermod -aG docker ubuntu || true

echo "=== 4. Cloning Repository ==="
if [ ! -d "$TARGET_DIR" ]; then
    git clone "$REPO_URL" "$TARGET_DIR"
else
    echo "Directory $TARGET_DIR already exists, skipping clone."
fi

cd "$TARGET_DIR"

echo "=============================================================================="
echo " Setup complete!"
echo " NEXT STEPS:"
echo " 1. Log out and back in (or run 'newgrp docker') so group permissions take effect."
echo " 2. Navigate to repository: cd $TARGET_DIR"
echo " 3. Create your production .env file: cp .env.example .env"
echo " 4. Edit .env with your real Render DB credentials and Elastic IP."
echo " 5. Start containers: docker compose up -d --build"
echo "=============================================================================="
