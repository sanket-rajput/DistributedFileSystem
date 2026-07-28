# Stage 1: Build static assets using Node.js
FROM node:20-alpine AS builder
WORKDIR /app

# Accept VITE_API_BASE_URL build argument (defaults to relative '/api/v1' for Nginx reverse-proxy)
ARG VITE_API_BASE_URL=/api/v1
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# Copy dependency manifests and install
COPY package.json ./
RUN npm install

# Copy application source code and build production bundle
COPY . .
RUN npm run build

# Stage 2: Serve static bundle via Nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Clean default assets and copy production dist build
RUN rm -rf ./*
COPY --from=builder /app/dist .

# Copy custom Nginx SPA and reverse proxy configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
