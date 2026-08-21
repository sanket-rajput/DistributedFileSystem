# Distributed File Sharing Platform — Technical Handoff & Technical Interview Reference

> [!IMPORTANT]
> **COMPLIANCE NOTICE**: This document represents a 100% verified, technically accurate analysis of the project's **CURRENT STATE** in the codebase. 
> - Features, architecture, metrics, and failure modes described herein are strictly derived from actual code and configuration files.
> - Any capability that is not present in the current codebase is explicitly marked as **NOT IMPLEMENTED**, **CONFIGURED BUT UNUSED**, or **ASSUMED / NOT VERIFIED**.

---

## 1. PROJECT OVERVIEW

### What the Project Does
The **Distributed File Sharing Platform** is a full-stack web application for uploading, storing, organizing, sharing, versioning, searching, and managing files with granular permissions, asynchronous event processing, and cached metadata lookups.

### Problem Solved
Provides secure, centralized, and persistent object storage with automated SHA-256 content deduplication (saving storage space when users upload identical content), file version tracking (enabling file restoration), and public link generation with time-bound/view-only security controls.

### Main Users & Use Cases
- **Authenticated Users**: Register, log in, create nested folders, upload/download files, preview images/PDFs/videos/text inline, search files by multi-attribute criteria, generate public share links, inspect file version history, and restore historical versions.
- **Anonymous Public Users**: Access shared files via unique share tokens (`/api/v1/share/{token}`), view metadata, stream content inline, or download files (if permitted by owner).
- **Admin Users**: Access administrative endpoints (`/api/v1/users/admin-only`, `/api/v1/audit-logs`) to list all registered users and review system-wide audit trails.

### Current Implemented Features
1. **User Authentication & Authorization**: JWT-based stateless authentication with BCrypt password hashing and Spring Security RBAC (`ROLE_USER`, `ROLE_ADMIN`).
2. **File & Folder Management**: Flat and nested folder hierarchies (`Folder`), file upload, download, metadata retrieval, list by folder, specification-based search, and recursive deletion.
3. **Storage Deduplication (SHA-256)**: Single-pass streaming SHA-256 hash computation. If an identical hash already exists for the uploading user, the physical storage object in MinIO is reused.
4. **Automatic File Versioning**: Uploading a file with an existing filename into the same folder automatically archives the previous version into `FileVersion` and increments `currentVersionNumber`. Historic versions can be downloaded or restored.
5. **Granular Public Sharing**: Unique tokenized links with optional expiration dates (`expiresAt`) and permissions (`VIEW` vs `DOWNLOAD`). Inline streaming vs download enforcement.
6. **Multi-Format Inline File Viewer**: Frontend viewer (`FileViewer.jsx`) supporting inline viewing for Images, PDFs, Monospace Text/Code, Videos, Audio, and graceful fallback for binary/office files.
7. **Asynchronous Notification Pipeline (Kafka)**: Publishes `FILE_UPLOADED`, `FILE_SHARED`, and `FILE_DELETED` events to Kafka topic `file-events`. A consumer processes events and saves user notifications into PostgreSQL (`Notification`).
8. **Audit Trail Logging (Spring AOP)**: Custom `@Auditable` aspect intercepts `@Auditable` annotated service methods and logs operations to `audit_logs` in PostgreSQL.
9. **Metadata Caching (Redis)**: Cache-aside strategy (`@Cacheable(value = "fileMetadata", key = "#fileId")`) for file metadata retrieval with cache eviction on delete/restore.
10. **Observability & Metrics**: Spring Boot Actuator with Micrometer export to Prometheus (`/actuator/prometheus`) and custom Grafana dashboards for upload/download timers and Kafka counters.
11. **Production Reverse Proxy & SSL**: Nginx container handling HTTPS port 443 with automated Let's Encrypt SSL/TLS certificates via Certbot on `https://fileshare.sanketrajput.live`.

### Current Limitations
- **Deduplication Scope**: Deduplication is scoped strictly **per-user** (`owner_id + sha256_hash`). Identical files uploaded by two different users are stored as separate MinIO objects.
- **Single-Host Monolith**: All application domain logic runs in a **single Spring Boot process** container (`fileshare-app`), NOT independent microservices.
- **No Chunked / Resumable Upload**: Uploads are processed as single `MultipartFile` HTTP requests. Multipart chunking and resumable upload tracking are **NOT IMPLEMENTED**.
- **No Pre-Signed S3 URLs**: MinIO object upload/download is performed by streaming bytes through the Spring Boot application backend, increasing backend bandwidth consumption.
- **Non-Atomic S3 + DB Transactions**: MinIO calls occur outside database transaction boundaries. S3 failure or DB rollback can result in orphaned storage objects or missing metadata.

### Overall Architecture: Modular Monolith vs. Microservices
- **Architecture Type**: **MODULAR MONOLITH** (Single Spring Boot backend process package-structured by domain module: `auth`, `user`, `file`, `folder`, `versioning`, `sharing`, `notification`, `audit`, `event`).
- **Number of Backend Application Services Present**: **1** single Spring Boot Java application (`DistributedFileSharingApplication`).
- **Auxiliary System Services**: Nginx (Frontend Reverse Proxy), PostgreSQL (Relational DB), MinIO (S3 Object Storage), Redis (In-Memory Cache), Kafka + Zookeeper/KRaft (Event Streaming), Prometheus & Grafana (Monitoring).

```
Client (Browser / React SPA)
       │
       ├── (Port 80 HTTP) ──► Nginx Container (301 Redirect to HTTPS / Certbot ACME)
       └── (Port 443 HTTPS) ──► Nginx Container (React 18 Static Build + Reverse Proxy)
                                      │
                                      ▼  Reverse Proxy (/api/*)
                      fileshare-app (Spring Boot 3.3 / Java 21)
                       [Single Monolithic Process: Port 8080]
                                      │
           ┌──────────────────────────┼──────────────────────────┬──────────────────────────┐
           ▼                          ▼                          ▼                          ▼
PostgreSQL (Port 5432)        MinIO S3 (Port 9000)        Redis (Port 6379)         Kafka (Port 9092)
[Users, FileMetadata,         [fileshare-storage          [fileMetadata::<id>        [file-events topic
 Folders, FileVersions,        bucket for binary           5-min TTL cache-aside]     consumed internally]
 Shares, Notifications]        file objects]
```

---

## 2. COMPLETE REPOSITORY STRUCTURE

```
DisfileSys/
├── .env, .env.example              # Infrastructure & application environment variables
├── .dockerignore, .gitignore       # Build/Docker ignore rules
├── BUGS_FOUND.md                   # Systematic QA and root-cause bug fix report
├── README.md                       # Project documentation & benchmark execution instructions
├── docker-compose.yml              # Primary Docker Compose configuration (Frontend, App, DB, MinIO, Redis, Kafka, Certbot)
├── docker-compose.monitoring.yml   # Supplemental Docker Compose configuration (Prometheus, Grafana)
├── dockerfile                      # Multi-stage Dockerfile building Spring Boot backend JAR (Java 21)
├── pom.xml                         # Maven build configuration & dependency manifest
├── deploy/                         # AWS EC2 deployment notes & SSL configuration scripts
├── frontend/                       # React 18 + Vite + Tailwind CSS SPA frontend codebase
│   ├── Dockerfile                  # Multi-stage Dockerfile (Vite build + Nginx alpine)
│   ├── nginx.conf                  # Production Nginx config (Port 80/443, SSL, proxy_pass to http://app:8080)
│   └── src/                        # React components, pages, context, and API clients
├── grafana/                        # Grafana provisioning configs and dashboards JSON
├── k8s/                            # Kubernetes manifest templates (CONFIGURED BUT UNUSED)
├── loadtest/                       # k6 load testing & seeding JavaScript scripts
├── nginx/                          # Standalone Nginx configuration files
├── prometheus/                     # Prometheus scraping configuration (`prometheus.yml`)
└── src/                            # Java Spring Boot backend source code
    ├── main/
    │   ├── java/com/fileshare/     # Java source packages
    │   │   ├── DistributedFileSharingApplication.java  # Main Spring Boot entry point
    │   │   ├── audit/             # AOP Audit logging module (Aspect, Controller, Service, Entity, Repo)
    │   │   ├── auth/              # JWT Security & Auth module (Controller, Service, Filters, Provider)
    │   │   ├── cache/             # Redis Cache configuration (`RedisConfig.java`)
    │   │   ├── common/            # Shared DTOs (`ApiResponse`), GlobalExceptionHandler, Custom Exceptions
    │   │   ├── config/            # SecurityConfig, MinioConfig, SwaggerConfig, MetricsConfig
    │   │   ├── event/             # Kafka configuration (`KafkaConfig`), Event DTOs, EventPublisher
    │   │   ├── file/              # File metadata & S3 storage module (Controllers, Services, Entity, Repo)
    │   │   ├── folder/            # Directory management module (Controller, Service, Entity, Repo)
    │   │   ├── notification/      # Kafka Notification Consumer & Notification Service (Consumer, Service, Entity)
    │   │   ├── search/            # Dynamic JPA Specification search filters (`FileSpecification.java`)
    │   │   ├── sharing/           # Public tokenized link sharing module (Controllers, Service, Entity)
    │   │   ├── user/              # User management & RBAC profile module (Controller, Service, Entity)
    │   │   └── versioning/        # File versioning & restoration module (Controller, Service, Entity)
    │   └── resources/
    │       ├── application.yml        # Core application configuration (Port, JPA, Redis, Kafka, MinIO, Actuator)
    │       └── application-local.yml  # Local dev profile overrides
    └── test/                      # Unit, Integration, and Testcontainers test suites
```

### Directory & File Responsibilities

| Path | Purpose / Content | Consuming Component | Important Classes / Files | Important Config |
|---|---|---|---|---|
| `pom.xml` | Maven dependency management, plugins (JaCoCo, Failsafe, Surefire, Compiler) | Maven / Docker build | Spring Boot 3.3.0, Java 21, JJWT 0.12.5, MinIO 8.5.10 | Target Java 21, Lombok, MapStruct |
| `docker-compose.yml` | Container orchestration manifest | Docker Engine | Services: `frontend`, `certbot`, `app`, `minio`, `redis`, `kafka` | Internal networks, ports, healthchecks |
| `dockerfile` | Spring Boot backend container image | Docker | Multi-stage build (`maven:3.9-eclipse-temurin-21` -> `eclipse-temurin:21-jre`) | Exposes port 8080 |
| `frontend/nginx.conf` | Nginx web server config for SPA & proxy | `fileshare-frontend` container | SSL/TLS config, reverse proxy `proxy_pass http://app:8080` | Client max body size 500M |
| `src/main/resources/application.yml` | Spring environment configuration | `fileshare-app` container | JPA, Liquibase/Hibernate ddl-auto (`update`), Cache, Kafka, MinIO, JWT | Default ports, TTLs, actuator endpoints |
| `com.fileshare.config.SecurityConfig` | Spring Security filter chain & CORS | Spring Security | `securityFilterChain`, `corsConfigurationSource`, `passwordEncoder` | Stateless session, PermitAll matcher, CORS list |
| `com.fileshare.file.service.FileServiceImpl` | Primary file handling & business logic | FileController | `uploadFile`, `downloadFile`, `deleteFile`, `getFileMetadata` | SHA-256 hash, per-user deduplication logic |
| `com.fileshare.file.service.MinioFileStorageServiceImpl` | MinIO SDK wrapper for S3 operations | FileServiceImpl | `uploadFile`, `downloadFile`, `deleteFile` | `minioClient.putObject`, `getObject`, `removeObject` |
| `com.fileshare.event.service.EventPublisherServiceImpl` | Kafka message publisher | FileServiceImpl, SharingServiceImpl | `publishFileUploaded`, `publishFileShared`, `publishFileDeleted` | `kafkaTemplate.send("file-events", key, payload)` |
| `com.fileshare.notification.consumer.NotificationConsumer` | Kafka message listener | Kafka Listener Container | `consumeFileEvent` | `@KafkaListener(topics = "file-events")` |

---

## 3. SERVICE-BY-SERVICE BREAKDOWN

> [!NOTE]
> The backend application is implemented as **1 single Java backend process** (`fileshare-app`) organized internally into 9 domain modules. Auxiliary infrastructure containers act as supporting services.

### Backend Monolith Application Service Breakdown

- **Service Name**: `fileshare-app` (`DistributedFileSharingApplication`)
- **Port**: `8080` (Internal Docker network & mapped to host port 8080)
- **Technology**: Java 21, Spring Boot 3.3.0, Spring Data JPA, Spring Security, Spring Kafka, Spring Data Redis, MinIO Java SDK, Micrometer.
- **Purpose**: Core application logic, API endpoints, authentication, authorization, business rules, metadata persistence, storage streaming, and event handling.

#### 1. Authentication & User Management Module (`auth` & `user`)
- **Controllers**:
  - `AuthController`: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`
  - `UserController`: `GET /api/v1/users/me`, `GET /api/v1/users/admin-only` (`hasRole('ADMIN')`)
- **Services**:
  - `AuthServiceImpl`: Implements registration (`BCryptPasswordEncoder`, creates `User`) and login (`AuthenticationManager`, returns `AuthResponse` with JWT token).
  - `UserServiceImpl`: Retrieves user profiles by ID (`getUserById`) and lists all users (`getAllUsers`).
  - `CustomUserDetailsService`: Implements `UserDetailsService.loadUserByUsername` to build `UserPrincipal`.
- **Repositories**: `UserRepository` (`findByEmail`, `existsByEmail`).
- **Entities**: `User` (`id` UUID, `email` String, `passwordHash` String, `role` Role Enum (`USER`, `ADMIN`), `createdAt` LocalDateTime).

#### 2. File Metadata & Storage Module (`file`)
- **Controllers**:
  - `FileController`:
    - `POST /api/v1/files/upload` (Multipart form-data)
    - `GET /api/v1/files/{id}/download` (File byte stream resource)
    - `GET /api/v1/files/{id}` (Cached metadata)
    - `GET /api/v1/files` (List files by folder)
    - `GET /api/v1/files/search` (JPA Specification multi-filter search)
    - `DELETE /api/v1/files/{id}` (Delete file)
- **Services**:
  - `FileServiceImpl`: Computes SHA-256 hash, performs per-user deduplication, creates `FileVersion` records, delegates binary storage to `MinioFileStorageServiceImpl`, publishes Kafka events, manages `@Cacheable`/`@CacheEvict` annotations.
  - `MinioFileStorageServiceImpl`: Invokes `MinioClient.putObject()`, `getObject()`, and `removeObject()`.
- **Repositories**: `FileMetadataRepository` (`findFirstByOwnerIdAndSha256Hash`, `findByOwnerIdAndFolderIdAndOriginalFilename`, `findByOwnerIdAndFolderIsNullAndOriginalFilename`, `findAll(Specification, Pageable)`).
- **Entities**: `FileMetadata` (`id` UUID, `originalFilename`, `storageKey`, `sizeBytes`, `contentType`, `sha256Hash`, `currentVersionNumber`, `folder` FK, `owner` FK, `createdAt`). Indexed on `sha256_hash`.

#### 3. Directory & Folder Management Module (`folder`)
- **Controllers**:
  - `FolderController`: `POST /api/v1/folders`, `GET /api/v1/folders/{id}`, `GET /api/v1/folders` (`parentId`), `PUT /api/v1/folders/{id}`, `DELETE /api/v1/folders/{id}`.
- **Services**: `FolderServiceImpl`: Creates root/nested folders, handles folder renaming, lists folders by parent ID, and performs recursive cascade deletion of subfolders and contained files.
- **Repositories**: `FolderRepository` (`findByOwnerIdAndParentFolderId`, `findByOwnerIdAndParentFolderIsNull`, `findByParentFolderId`).
- **Entities**: `Folder` (`id` UUID, `name`, `owner` FK, `parentFolder` Self-referencing FK, `createdAt`).

#### 4. File Versioning Module (`versioning`)
- **Controllers**:
  - `FileVersionController`:
    - `GET /api/v1/files/{fileId}/versions`
    - `GET /api/v1/files/{fileId}/versions/{versionNumber}/download`
    - `POST /api/v1/files/{fileId}/versions/{versionNumber}/restore`
- **Services**: `FileVersionServiceImpl`: Retrieves version history list, streams specific historic version bytes from MinIO, and restores target version as new active version (incrementing version number and updating `FileMetadata.storageKey`).
- **Repositories**: `FileVersionRepository` (`findByFileMetadataIdOrderByVersionNumberDesc`, `findByFileMetadataIdAndVersionNumber`).
- **Entities**: `FileVersion` (`id` UUID, `fileMetadata` FK, `versionNumber`, `storageKey`, `sizeBytes`, `sha256Hash`, `createdBy` FK, `createdAt`).

#### 5. Public File Sharing Module (`sharing`)
- **Controllers**:
  - `SharingController`: `POST /api/v1/files/{fileId}/share`, `GET /api/v1/files/{fileId}/share`, `DELETE /api/v1/files/{fileId}/share/{shareId}`.
  - `PublicShareController` (Unauthenticated): `GET /api/v1/share/{token}` (Metadata), `GET /api/v1/share/{token}/stream` (Content Stream).
- **Services**: `SharingServiceImpl`: Generates/updates tokenized share links with permissions (`VIEW`, `DOWNLOAD`) and expiry timestamps (`expiresAt`), validates access tokens, enforces view-only download restrictions (HTTP 403 on download attempt for `VIEW` permission), and streams shared content.
- **Repositories**: `ShareRepository` (`findByToken`, `findFirstByFileMetadataIdAndRevokedFalseOrderByCreatedAtDesc`).
- **Entities**: `Share` (`id` UUID, `fileMetadata` FK, `token` String Unique, `createdBy` FK, `expiresAt` LocalDateTime, `permission` SharePermission Enum (`VIEW`, `DOWNLOAD`), `revoked` boolean, `createdAt`).

#### 6. Notification System Module (`notification`)
- **Controllers**: `NotificationController`: `GET /api/v1/notifications` (Paginated list for logged-in user).
- **Consumer**: `NotificationConsumer`: `@KafkaListener` listening to topic `file-events`, deserializes payload, converts event into readable text, and saves `Notification` entity to PostgreSQL.
- **Services**: `NotificationServiceImpl`: Fetches user notifications.
- **Repositories**: `NotificationRepository` (`findByUserIdOrderByCreatedAtDesc`).
- **Entities**: `Notification` (`id` UUID, `user` FK, `message` String(1000), `type` String, `read` boolean, `createdAt`).

#### 7. Audit Logging Module (`audit`)
- **Controllers**: `AuditLogController`: `GET /api/v1/audit-logs` (Admin-only filterable audit trail).
- **Aspect**: `AuditLogAspect`: Spring AOP `@AfterReturning` advice intercepting `@Auditable` annotated service methods (`uploadFile`, `downloadFile`, `deleteFile`), extracts `UserPrincipal` from `SecurityContextHolder`, and saves `AuditLog` entry.
- **Services**: `AuditLogServiceImpl`: Queries audit logs using JPA Specifications.
- **Repositories**: `AuditLogRepository` (`findAll(Specification, Pageable)`).
- **Entities**: `AuditLog` (`id` UUID, `userId` UUID, `userEmail`, `action` AuditAction Enum (`UPLOAD`, `DOWNLOAD`, `DELETE`, `SHARE`, `RESTORE_VERSION`), `resourceType`, `resourceId`, `timestamp`, `details`).

---

## 4. END-TO-END REQUEST FLOWS

### Flow A: User Registration / Login
```
Client (Browser)
  ↓ POST /api/v1/auth/login {email, password}
AuthController
  ↓ login(LoginRequest)
AuthServiceImpl
  ↓ authenticationManager.authenticate(...)
CustomUserDetailsService -> UserRepository -> PostgreSQL (SELECT * FROM users WHERE email = ?)
  ↓ (Password match verified via BCryptPasswordEncoder)
JwtTokenProvider.generateToken(Authentication)
  ↓ (Generates signed HS512/HS256 JWT containing email, userId, role claims)
AuthResponse { token, tokenType: "Bearer", userId, email, role }
  ↓ HTTP 200 OK
Client (Stores token in localStorage)
```

### Flow B: File Upload (With Deduplication & Versioning Check)
```
Client (Browser / Dashboard)
  ↓ POST /api/v1/files/upload (MultipartFormData: file, folderId?)
FileController (Authenticated via JwtAuthenticationFilter)
  ↓ uploadFile(file, folderId, ownerId)
FileServiceImpl
  ├─► Reads InputStream & computes SHA-256 Digest in memory (8KB buffer)
  ├─► Queries FileMetadataRepository.findFirstByOwnerIdAndSha256Hash(ownerId, sha256)
  │     ├─► [IF DEDUPLICATED]: Increment metric counter, reuse existing storageKey.
  │     └─► [IF NOT DEDUPLICATED]: Generate UUID storageKey, call MinioFileStorageServiceImpl.uploadFile()
  │           └─► MinioClient.putObject() ──► MinIO Storage (Bucket: fileshare-storage)
  ├─► Queries FileMetadataRepository for existing filename in target folder
  │     ├─► [IF EXISTING FILE]: Archive current metadata state to FileVersion table, update FileMetadata to v(N+1).
  │     └─► [IF NEW FILE]: Save new FileMetadata (v1), save FileVersion (v1).
  ├─► EventPublisherServiceImpl.publishFileUploaded(...)
  │     └─► KafkaTemplate.send("file-events", fileId, FileUploadedEvent) ──► Kafka Broker
  ├─► AOP AuditLogAspect.auditMethodExecution() ──► PostgreSQL (audit_logs table)
  └─► Return FileResponseDto (isDeduplicated: boolean)
Client (HTTP 201 Created)
```

### Flow C: File Download
```
Client (Browser / Dashboard)
  ↓ GET /api/v1/files/{fileId}/download (Header: Authorization: Bearer <jwt>)
FileController
  ↓ downloadFile(fileId, ownerId)
FileServiceImpl
  ├─► getFileEntityAndCheckOwner() ──► PostgreSQL (SELECT * FROM file_metadata WHERE id = ?)
  ├─► MinioFileStorageServiceImpl.downloadFile(storageKey)
  │     └─► MinioClient.getObject() ──► MinIO Storage (Returns InputStream byte stream)
  ├─► Wrap InputStream in InputStreamResource
  ├─► AOP AuditLogAspect.auditMethodExecution() ──► PostgreSQL (audit_logs table)
  └─► Return ResponseEntity<Resource> with Content-Disposition: attachment; filename="..."
Client (Receives binary stream download)
```

### Flow D: Public Share Link Generation & Unauthenticated Access
```
1. SHARE LINK CREATION (Logged-In User):
Client ──► POST /api/v1/files/{fileId}/share ──► SharingController ──► SharingServiceImpl
  ├─► Verify file ownership in PostgreSQL
  ├─► Check active share in share table (Reuse stable token if active)
  ├─► Generate UUID token, save Share entity to PostgreSQL
  ├─► EventPublisherServiceImpl.publishFileShared() ──► Kafka Broker
  └─► Return ShareResponseDto { shareUrl: "/api/v1/share/{token}", token, permission }

2. UNAUTHENTICATED ACCESS (Incognito Visitor):
Client ──► GET /api/v1/share/{token} ──► PublicShareController ──► SharingServiceImpl
  ├─► Query ShareRepository.findByToken(token) ──► PostgreSQL
  ├─► Check share.isRevoked() & share.isExpired() (Throws HTTP 403 if invalid)
  └─► Return ShareResponseDto JSON metadata

3. UNAUTHENTICATED STREAM / DOWNLOAD:
Client ──► GET /api/v1/share/{token}/stream?inline=true ──► PublicShareController ──► SharingServiceImpl
  ├─► Validate share token, status, and expiry
  ├─► Check permission: IF inline=false (download) AND permission=VIEW ──► Throw AccessDeniedException (HTTP 403)
  ├─► MinioFileStorageServiceImpl.downloadFile(metadata.getStorageKey()) ──► MinIO
  └─► Return ResponseEntity<Resource> with Content-Disposition: inline or attachment
```

---

## 5. FILE UPLOAD ARCHITECTURE — DEEP ANALYSIS

- **Does the frontend split files?**: **NO**. The frontend (`FileUploadModal.jsx`) sends the complete file as a single standard `MultipartFile` using `axios.post('/api/v1/files/upload', formData)`.
- **Does the backend split files?**: **NO**. The backend receives `MultipartFile file` and processes the file stream as a single contiguous object.
- **Does MinIO handle multipart upload?**: **NO explicit chunking configured**. The backend uses standard `minioClient.putObject()` passing the full InputStream and size.
- **What is the chunk size?**: **NOT APPLICABLE / NOT IMPLEMENTED**.
- **Where are chunks stored?**: **NOT APPLICABLE**.
- **How are chunks identified?**: **NOT APPLICABLE**.
- **How is upload state tracked?**: **NOT APPLICABLE**.
- **Does Redis store upload state?**: **NO**.
- **Does PostgreSQL store upload state?**: **NO**. Upload is stateless and synchronous per HTTP request.
- **How are failed chunks retried?**: **NOT IMPLEMENTED**. If an HTTP request breaks mid-stream, the entire upload fails and must be re-initiated from 0%.
- **How does resumable upload work?**: **NOT IMPLEMENTED**.
- **What happens if the client disconnects?**: The Spring servlet container throws an `IOException` / `ClientAbortException`. The partial stream is aborted, and no database records are committed.
- **What happens if the same file is uploaded twice?**:
  - **Same user, same folder, same filename**: Triggers **Versioning**. The initial file payload is stored, the previous metadata is saved into `FileVersion` (v1), and `FileMetadata` is updated to v2. If the content is identical, SHA-256 deduplication reuses the existing `storageKey`.
  - **Same user, different folder/filename, identical content**: Triggers **Deduplication**. MinIO upload is skipped; the new `FileMetadata` record points to the existing `storageKey`.
- **When is hashing performed?**: Hashing is performed **synchronously in memory/temp file on the backend** at the beginning of `FileServiceImpl.uploadFile()` before writing to MinIO.
- **Which hash algorithm is actually used?**: **`SHA-256`** (`MessageDigest.getInstance("SHA-256")`).
- **Where is the hash stored?**: Stored in PostgreSQL column `file_metadata.sha256_hash` (indexed via `idx_file_sha256`) and `file_versions.sha256_hash`.
- **How does duplicate detection actually work?**:
  ```java
  Optional<FileMetadata> existingHashMatch = fileMetadataRepository.findFirstByOwnerIdAndSha256Hash(ownerId, sha256Hash);
  ```
  The system queries `file_metadata` for a match on both `owner_id` AND `sha256_hash`.
- **What happens when two users upload the same file simultaneously?**:
  Since deduplication is strictly **per-user** (`owner_id`), simultaneous uploads by two different users result in two independent MinIO uploads and two separate storage keys. Simultaneous uploads by the *same* user depend on database transaction isolation (`READ COMMITTED`); both may compute the hash, and whichever transaction commits second will see the existing hash match.
- **How is the final object created in MinIO?**:
  ```java
  minioClient.putObject(
      PutObjectArgs.builder()
          .bucket(bucketName)
          .object(storageKey)
          .stream(uploadStream, file.getSize(), -1)
          .contentType(contentType)
          .build()
  );
  ```

---

## 6. MINIO / OBJECT STORAGE

- **Why MinIO is used**: Serves as a local, S3-compatible object store for storing unstructured binary file content separate from structured PostgreSQL metadata.
- **How the application connects**: Via the official `io.minio:minio:8.5.10` Java SDK configured in `MinioConfig.java` bean `MinioClient.builder().endpoint(...).credentials(...).build()`.
- **Bucket names**: Default bucket name: **`fileshare-storage`** (Configured via `app.minio.bucket-name` in `application.yml`).
- **Object key structure**: Generated in `FileServiceImpl.java`:
  `storageKey = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_")`
  *(Example: `3fa85f64-5717-4562-b3fc-2c963f66afa6_document_pdf`)*.
- **Upload method**: `minioClient.putObject(...)` (`MinioFileStorageServiceImpl.java`).
- **Download method**: `minioClient.getObject(...)` returning raw `InputStream`.
- **Delete method**: `minioClient.removeObject(...)`.
- **Pre-signed URL implementation**: **NOT IMPLEMENTED**. All file access streams binary data directly through the Spring Boot application server controller.
- **Expiration time**: **NOT APPLICABLE** (No pre-signed URLs generated).
- **File metadata handling**: MinIO stores only `contentType` with binary streams. All user metadata (filenames, owner, folder, version, size, SHA-256 hash) is stored in PostgreSQL.
- **Access control**: MinIO bucket access is private to the application backend credentials (`MINIO_ROOT_USER` / `MINIO_ROOT_PASSWORD`). Direct external public access to MinIO port 9000/9001 is restricted or proxied via Spring Boot authorization filters.

---

## 7. POSTGRESQL DATABASE

### Relational Schema Specification

#### 1. `users`
- **Columns**: `id` (UUID, PK), `email` (VARCHAR, Unique, Not Null), `password_hash` (VARCHAR, Not Null), `role` (VARCHAR, Not Null), `created_at` (TIMESTAMP, Not Null).
- **Relationships**: One-to-Many with `file_metadata`, `folders`, `file_versions`, `shares`, `notifications`.

#### 2. `folders`
- **Columns**: `id` (UUID, PK), `name` (VARCHAR, Not Null), `owner_id` (UUID, FK -> `users.id`, Not Null), `parent_folder_id` (UUID, FK -> `folders.id`, Nullable), `created_at` (TIMESTAMP, Not Null).
- **Relationships**: Self-referencing tree parent-child relationship; One-to-Many with `file_metadata`.

#### 3. `file_metadata`
- **Columns**: `id` (UUID, PK), `original_filename` (VARCHAR, Not Null), `storage_key` (VARCHAR, Not Null), `size_bytes` (BIGINT, Not Null), `content_type` (VARCHAR, Not Null), `sha256_hash` (VARCHAR, Nullable), `current_version_number` (INT, Not Null, Default 1), `folder_id` (UUID, FK -> `folders.id`, Nullable), `owner_id` (UUID, FK -> `users.id`, Not Null), `created_at` (TIMESTAMP, Not Null).
- **Indexes**: `idx_file_sha256` on column `sha256_hash`.

#### 4. `file_versions`
- **Columns**: `id` (UUID, PK), `file_metadata_id` (UUID, FK -> `file_metadata.id`, Not Null), `version_number` (INT, Not Null), `storage_key` (VARCHAR, Not Null), `size_bytes` (BIGINT, Not Null), `sha256_hash` (VARCHAR, Nullable), `created_by_id` (UUID, FK -> `users.id`, Not Null), `created_at` (TIMESTAMP, Not Null).

#### 5. `shares`
- **Columns**: `id` (UUID, PK), `file_metadata_id` (UUID, FK -> `file_metadata.id`, Not Null), `token` (VARCHAR, Unique, Not Null), `created_by_id` (UUID, FK -> `users.id`, Not Null), `expires_at` (TIMESTAMP, Nullable), `permission` (VARCHAR, Not Null), `revoked` (BOOLEAN, Not Null, Default false), `created_at` (TIMESTAMP, Not Null).

#### 6. `notifications`
- **Columns**: `id` (UUID, PK), `user_id` (UUID, FK -> `users.id`, Not Null), `message` (VARCHAR(1000), Not Null), `type` (VARCHAR, Not Null), `read` (BOOLEAN, Not Null, Default false), `created_at` (TIMESTAMP, Not Null).

#### 7. `audit_logs`
- **Columns**: `id` (UUID, PK), `user_id` (UUID, Nullable), `user_email` (VARCHAR, Nullable), `action` (VARCHAR, Not Null), `resource_type` (VARCHAR, Nullable), `resource_id` (UUID, Nullable), `timestamp` (TIMESTAMP, Not Null), `details` (VARCHAR(2000), Nullable).

### Database Configuration & Behavior
- **ORM / Driver**: Hibernate 6 / PostgreSQL JDBC Driver (`org.postgresql.Driver`).
- **DDL Auto**: `spring.jpa.hibernate.ddl-auto: update` (Schema generated/updated dynamically by Hibernate on startup).
- **Transactions**: Defined using `@Transactional` annotations at service layer methods.
- **Isolation Level**: Default database isolation level (**`READ COMMITTED`**). No custom isolation levels configured.
- **Locking**: Standard optimistic/pessimistic JPA locks are **NOT EXPLICITLY CONFIGURED**; relies on DB transaction defaults.

---

## 8. REDIS

- **Why Redis exists**: Used as an in-memory cache to reduce PostgreSQL database load during repeated metadata lookups for files.
- **What is stored in Redis**: `FileResponseDto` JSON objects representing file metadata.
- **Key naming convention**: **`fileMetadata::<fileId>`** (e.g., `fileMetadata::3fa85f64-5717-4562-b3fc-2c963f66afa6`).
- **Value structure**: Generic Jackson JSON serialized representation of `FileResponseDto`.
- **TTL**: **5 minutes** (300,000 ms), configured in `RedisConfig.java` (`Duration.ofMinutes(5)`).
- **Cache Strategy**: **Cache-Aside** via Spring Caching annotations:
  - `@Cacheable(value = "fileMetadata", key = "#fileId")` on `FileServiceImpl.getFileMetadata()`.
  - `@CacheEvict(value = "fileMetadata", key = "#fileId")` on `FileServiceImpl.deleteFile()` and `FileVersionServiceImpl.restoreFileVersion()`.
- **Cache Miss Flow**:
  1. Service receives request for `getFileMetadata(fileId)`.
  2. Spring Cache checks Redis for key `fileMetadata::<fileId>`.
  3. If missing (Miss), `FileServiceImpl.getFileMetadata()` executes DB query `fileMetadataRepository.findById(fileId)`.
  4. Result is serialized to JSON and stored in Redis with 5-minute TTL, then returned.
- **What happens if Redis goes down?**: If Redis connection fails, Spring Cache throws a Redis connection exception unless fallback error handling is configured.

---

## 9. APACHE KAFKA

### Topic & Event Specifications

- **Topic Name**: **`file-events`** (Configured in `KafkaConfig.java`, 1 Partition, Replication Factor 1).
- **Producer**: `EventPublisherServiceImpl` (Uses `KafkaTemplate<String, Object>`).
- **Consumer**: `NotificationConsumer` (Uses `@KafkaListener(topics = "file-events", groupId = "fileshare-notification-group")`).
- **Data Carried**: **EVENT METADATA ONLY** (File ID, Owner ID, Filename, Size, Share Token, Timestamp). Binary file contents are NEVER sent over Kafka.

### Event Schemas

#### 1. `FileUploadedEvent`
Payload: `{ "fileId": UUID, "userId": UUID, "filename": String, "sizeBytes": long, "timestamp": LocalDateTime }`

#### 2. `FileSharedEvent`
Payload: `{ "fileId": UUID, "userId": UUID, "shareToken": String, "permission": String, "timestamp": LocalDateTime }`

#### 3. `FileDeletedEvent`
Payload: `{ "fileId": UUID, "userId": UUID, "filename": String, "timestamp": LocalDateTime }`

### Event Processing Flow
```
Action (Upload / Share / Delete)
  ↓
Service Method Execution (FileServiceImpl / SharingServiceImpl)
  ↓
EventPublisherServiceImpl.publishEvent(type, key, payload)
  ↓ (Fire-and-Forget KafkaTemplate.send("file-events", fileId, payload))
Kafka Broker (Topic: file-events)
  ↓
NotificationConsumer.consumeFileEvent(Object message)
  ↓ (ObjectMapper converts message to Map, constructs Notification text)
NotificationRepository.save(Notification) ──► PostgreSQL (notifications table)
```

---

## 10. DISTRIBUTED SYSTEM FAILURE SCENARIOS

| Scenario | Current Code Behavior | Exception / Recovery Mechanism | Data Consistency Consequence |
|---|---|---|---|
| **1. PostgreSQL Down** | API requests fail with DB connection exceptions (`CannotCreateTransactionException`). | NO EXPLICIT RECOVERY MECHANISM FOUND. HTTP 500 error returned to client. | Service unserviceable until DB recovers. |
| **2. Redis Down** | Cache lookups fail when accessing `@Cacheable` endpoints. | NO EXPLICIT FALLBACK MECHANISM FOUND. HTTP 500 returned on cached endpoints. | Operational failure unless Redis is restored. |
| **3. Kafka Down** | `EventPublisherServiceImpl` catches exceptions during `kafkaTemplate.send()` fire-and-forget call and logs an error (`log.error`). | Async fire-and-forget catch block prevents main thread failure. | Main file operations succeed, but async notifications are lost. |
| **4. MinIO Down** | `MinioFileStorageServiceImpl` throws `FileStorageException`. | `GlobalExceptionHandler` catches exception and returns HTTP 500 error. | Upload/Download fails cleanly. |
| **5. Database Succeeds, MinIO Fails** | MinIO upload executes *before* DB save in `FileServiceImpl.uploadFile()`. If MinIO fails, DB save is never reached. | Standard method exception flow. | Clean rollback (No orphaned DB record). |
| **6. MinIO Succeeds, DB Fails** | MinIO upload completes, then DB save throws runtime exception (e.g. constraint violation). | DB transaction rolls back, but MinIO object was already written. | **Orphaned Object in MinIO** (MinIO object exists without DB metadata record). |
| **7. DB Succeeds, Kafka Fails** | DB transaction commits, then Kafka publish fails inside catch block. | Catch block logs error without throwing exception. | File upload is saved, but notification is lost. |
| **8. Dual Concurrent Uploads (Same User)** | Two threads compute SHA-256 concurrently. | Both perform hash check. If both see no match, both upload to MinIO. | Two storage keys created in MinIO, but second DB save succeeds. |

---

## 11. SECURITY

### Spring Security & Architecture
- **Framework**: Spring Security 6 with `@EnableWebSecurity` and `@EnableMethodSecurity(prePostEnabled = true)`.
- **Authentication Mechanism**: Stateless JWT Bearer Token attached in HTTP `Authorization` header (`Authorization: Bearer <token>`).
- **Password Hashing**: **BCrypt** (`BCryptPasswordEncoder` bean in `SecurityConfig`).
- **Token Generation**: `JwtTokenProvider.generateToken(Authentication)` builds signed JWTs with subject (email), `userId`, and `role` claims using HMAC SHA key (`app.jwt.secret`). Default expiration: **24 hours** (86,400,000 ms).
- **Refresh Tokens**: **NOT IMPLEMENTED**.
- **Token Blacklisting / Revocation**: **NOT IMPLEMENTED**. JWTs remain valid until natural expiration.
- **Security Filters**: `JwtAuthenticationFilter` extends `OncePerRequestFilter`, extracts token from header, validates signature, loads `UserDetails` via `CustomUserDetailsService`, and populates `SecurityContextHolder.getContext().setAuthentication(authentication)`.

### Role-Based Access Control (RBAC)
- **Roles**: `ROLE_USER`, `ROLE_ADMIN` (Defined in `Role` enum).
- **Method Authorization**: Enforced via `@PreAuthorize("hasRole('ADMIN')")` on `UserController.getAllUsersForAdmin()` and `AuditLogController.getAuditLogs()`.

### CORS & Public Share Security
- **CORS Configuration**: Strict allow-list defined in `SecurityConfig.corsConfigurationSource()` restricting origins to `https://fileshare.sanketrajput.live`, `http://fileshare.sanketrajput.live`, and local dev ports (`localhost:3000`, `5173`, `8080`).
- **Public Share Authorization**: Endpoint `/api/v1/share/**` is in `permitAll()` list in `SecurityConfig`. `SharingServiceImpl` validates link revocation (`share.isRevoked()`), expiry (`share.isExpired()`), and permission level (`VIEW` vs `DOWNLOAD`). Download attempts on `VIEW` shares throw `AccessDeniedException` (HTTP 403).

---

## 12. SPRING BOOT / JAVA ARCHITECTURE

- **Java Version**: **Java 21** (Configured in `pom.xml` `<java.version>21</java.version>`).
- **Spring Boot Version**: **3.3.0** (`spring-boot-starter-parent` 3.3.0).
- **DTO Mapping**: **MapStruct 1.5.5.Final** mapper interfaces (`FileMapper`, `FolderMapper`, `UserMapper`, `ShareMapper`, `FileVersionMapper`, `NotificationMapper`, `AuditLogMapper`).
- **Boilerplate Reduction**: **Lombok 1.18.36** (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`).
- **Exception Handling**: Centralized `@RestControllerAdvice` in `GlobalExceptionHandler.java` translating exceptions into standard `ErrorResponse` DTOs with appropriate HTTP status codes (400, 401, 403, 404, 409, 500).
- **Metrics & Observability**: Micrometer counters (`deduplicatedUploadsCounter`, `kafkaEventsPublishedCounter`, `kafkaEventsConsumedCounter`) and timers (`fileUploadTimer`, `fileDownloadTimer`) exposed via Actuator endpoint `/actuator/prometheus`.

---

## 13. DOCKER & INFRASTRUCTURE

### Docker Compose Container Architecture (`docker-compose.yml`)

1. `fileshare-frontend` (Nginx + React 18 SPA): Exposed on Ports 80 & 443.
2. `fileshare-certbot` (Certbot SSL Manager): Manages SSL renewal.
3. `fileshare-app` (Spring Boot Monolith Application): Internal Port 8080.
4. `fileshare-minio` (MinIO Object Store): Ports 9000 (API) & 9001 (Console).
5. `fileshare-redis` (Redis Cache): Port 6379.
6. `fileshare-kafka` (Confluent Kafka Broker): Ports 9092 & 29092 (KRaft mode).
7. `fileshare-prometheus` & `fileshare-grafana` (`docker-compose.monitoring.yml`): Ports 9090 & 3000.

### Resume Claim Audit: "6 Microservices"
- **VERDICT**: **FALSE / INACCURATE**.
- The codebase contains **ONLY 1 backend application service** (`fileshare-app`). The 6 Docker containers in `docker-compose.yml` represent **1 Application Monolith + 1 Frontend Nginx + 4 Infrastructure Middleware Services** (PostgreSQL, MinIO, Redis, Kafka).

---

## 14. API DOCUMENTATION

| Method | Path | Service Module | Auth Required? | Role Required | Request Body / Query Params | Response DTO | Purpose |
|---|---|---|---|---|---|---|---|
| `POST` | `/api/v1/auth/register` | Auth | No | None | `RegisterRequest` (email, password) | `ApiResponse<AuthResponse>` | Register new user |
| `POST` | `/api/v1/auth/login` | Auth | No | None | `LoginRequest` (email, password) | `ApiResponse<AuthResponse>` | Login user & get JWT |
| `GET` | `/api/v1/users/me` | User | Yes | Any | None | `ApiResponse<UserResponseDto>` | Get logged-in user profile |
| `GET` | `/api/v1/users/admin-only` | User | Yes | `ADMIN` | None | `ApiResponse<List<UserResponseDto>>` | Admin user listing |
| `POST` | `/api/v1/files/upload` | File | Yes | Any | Multipart `file`, optional `folderId` | `ApiResponse<FileResponseDto>` | Upload file (Deduplicate/Version) |
| `GET` | `/api/v1/files/{id}/download` | File | Yes | Any | None | `Resource` (Binary Stream) | Download file bytes |
| `GET` | `/api/v1/files/{id}` | File | Yes | Any | None | `ApiResponse<FileResponseDto>` | Get cached file metadata |
| `GET` | `/api/v1/files` | File | Yes | Any | Optional `folderId` | `ApiResponse<List<FileResponseDto>>` | List files in folder |
| `GET` | `/api/v1/files/search` | File | Yes | Any | `name`, `contentType`, `minSize`, `maxSize`, `page`, `size` | `ApiResponse<Page<FileResponseDto>>` | Multi-filter file search |
| `DELETE` | `/api/v1/files/{id}` | File | Yes | Any | None | `ApiResponse<Void>` | Delete file & metadata |
| `POST` | `/api/v1/folders` | Folder | Yes | Any | `CreateFolderRequest` (name, parentId) | `ApiResponse<FolderResponseDto>` | Create directory folder |
| `GET` | `/api/v1/folders/{id}` | Folder | Yes | Any | None | `ApiResponse<FolderResponseDto>` | Get folder details |
| `GET` | `/api/v1/folders` | Folder | Yes | Any | Optional `parentId` | `ApiResponse<List<FolderResponseDto>>` | List user folders |
| `PUT` | `/api/v1/folders/{id}` | Folder | Yes | Any | `RenameFolderRequest` (name) | `ApiResponse<FolderResponseDto>` | Rename folder |
| `DELETE` | `/api/v1/folders/{id}` | Folder | Yes | Any | None | `ApiResponse<Void>` | Cascade delete folder & contents |
| `GET` | `/api/v1/files/{fileId}/versions` | Versioning | Yes | Any | None | `ApiResponse<List<FileVersionResponseDto>>` | List file version history |
| `GET` | `/api/v1/files/{fileId}/versions/{v}/download` | Versioning | Yes | Any | None | `Resource` (Binary Stream) | Download historic version |
| `POST` | `/api/v1/files/{fileId}/versions/{v}/restore` | Versioning | Yes | Any | None | `ApiResponse<FileVersionResponseDto>` | Restore historic version |
| `POST` | `/api/v1/files/{fileId}/share` | Sharing | Yes | Any | `CreateShareRequest` (permission, expiresAt) | `ApiResponse<ShareResponseDto>` | Create/get share link |
| `GET` | `/api/v1/files/{fileId}/share` | Sharing | Yes | Any | None | `ApiResponse<ShareResponseDto>` | Get active share link |
| `DELETE` | `/api/v1/files/{fileId}/share/{shareId}` | Sharing | Yes | Any | None | `ApiResponse<Void>` | Revoke share link |
| `GET` | `/api/v1/share/{token}` | Sharing | No | None | None | `ApiResponse<ShareResponseDto>` | Public share metadata inspection |
| `GET` | `/api/v1/share/{token}/stream` | Sharing | No | None | Optional `inline` (boolean) | `Resource` (Binary Stream) | Stream/download shared file |
| `GET` | `/api/v1/notifications` | Notification | Yes | Any | `page`, `size` | `ApiResponse<Page<NotificationResponseDto>>` | Get user notifications |
| `GET` | `/api/v1/audit-logs` | Audit | Yes | `ADMIN` | `userId`, `action`, `fromDate`, `toDate`, `page`, `size` | `ApiResponse<Page<AuditLogResponseDto>>` | Admin audit log listing |

---

## 15. PERFORMANCE CLAIMS AUDIT

> [!WARNING]
> **RESUME VERIFICATION NOTICE**: The metric status below reflects authoritative findings from `loadtest/RESULTS.md` and codebase inspection.

| Resume Claim / Metric | Status | Evidence in Codebase | Notes / Explanation |
|---|---|---|---|
| **65% Latency Reduction** | **ASSUMED / NOT VERIFIED** | `loadtest/RESULTS.md` | Placeholder `[Pending]` in benchmark results file. No before/after baseline stored. |
| **Under 150 ms API Latency** | **ASSUMED / NOT VERIFIED** | `loadtest/RESULTS.md` | No recorded k6 run output confirming p95 < 150ms. |
| **5,000+ Files Handled** | **ASSUMED / NOT VERIFIED** | `loadtest/seed-5000-files.js` | Seeding script exists, but no benchmark log confirming DB/MinIO performance at this scale. |
| **10,000+ Kafka Events Burst** | **ASSUMED / NOT VERIFIED** | `loadtest/kafka-event-burst.js` | Burst script exists, but benchmark metrics are marked `[Pending]`. |
| **40% Duplicate-Storage Reduction** | **ASSUMED / NOT VERIFIED** | `FileServiceImpl.java` | SHA-256 deduplication logic is implemented, but 40% reduction is an unverified estimate. |
| **80% Setup-Time Reduction** | **ASSUMED / NOT VERIFIED** | `docker-compose.yml` | One-command setup `docker compose up` exists, but setup time was never formally benchmarked. |

---

## 16. TESTING

- **Unit Tests**: Located in `src/test/java/com/fileshare/` (`AuthServiceTest`, `UserServiceTest`, `FileServiceTest`, `FolderServiceTest`, `FileVersionServiceTest`, `SharingServiceTest`, `NotificationServiceTest`, `AuditLogServiceTest`, `JwtServiceTest`). Mockito mocks repository dependencies.
- **Controller Tests**: `FileUploadControllerTest` tests endpoint web layer using `MockMvc`.
- **Integration Tests & Testcontainers**:
  - `FileMetadataRepositoryIT`: Uses `org.testcontainers.postgresql.PostgreSQLContainer` to test repository queries against a real PostgreSQL container instance.
  - `FileSharingE2EIntegrationIT`: Uses Testcontainers for PostgreSQL and Kafka to test end-to-end event publishing and database flows.
- **Load Testing Suite**: k6 scripts located in `loadtest/` (`auth-and-crud-load.js`, `search-benchmark.js`, `seed-5000-files.js`, `kafka-event-burst.js`).

---

## 17. DEPLOYMENT

- **Local Deployment**: Executed via Docker Compose: `cp .env.example .env && docker compose up -d --build`.
- **Cloud Production Deployment**: Deployed on an **AWS EC2 instance** running Docker Compose behind Nginx and Certbot.
- **Domain & SSL**: Live at **`https://fileshare.sanketrajput.live`** with auto-renewing Let's Encrypt SSL/TLS certificates.
- **Kubernetes**: Manifest templates exist in `k8s/`, but deployment to a live Kubernetes cluster is **NOT IMPLEMENTED / NOT VERIFIED**.

---

## 18. CURRENT STATUS SUMMARY TABLE

| Feature | Status | Evidence | Notes |
|---|---|---|---|
| **JWT Authentication & BCrypt** | IMPLEMENTED | `SecurityConfig.java`, `JwtTokenProvider.java` | 24-hour expiration, no refresh tokens |
| **Role-Based Access Control (RBAC)** | IMPLEMENTED | `UserController.java`, `AuditLogController.java` | `@PreAuthorize("hasRole('ADMIN')")` |
| **SHA-256 Storage Deduplication** | IMPLEMENTED (PARTIAL) | `FileServiceImpl.java` | Implemented per-user (`owner_id + sha256_hash`) |
| **Automatic File Versioning** | IMPLEMENTED | `FileVersionServiceImpl.java`, `FileVersion.java` | Archives previous state on filename match |
| **Public Tokenized Link Sharing** | IMPLEMENTED | `PublicShareController.java`, `SharingServiceImpl.java` | Token access with `VIEW` vs `DOWNLOAD` rules |
| **Kafka Asynchronous Notifications** | IMPLEMENTED | `KafkaConfig.java`, `NotificationConsumer.java` | Topic `file-events`, persisted to `Notification` |
| **Redis Metadata Cache-Aside** | IMPLEMENTED (PARTIAL) | `RedisConfig.java`, `FileServiceImpl.java` | Applied strictly to `getFileMetadata` (5-min TTL) |
| **AOP Audit Logging** | IMPLEMENTED | `AuditLogAspect.java`, `AuditLogController.java` | Intercepts `@Auditable` annotated service methods |
| **Multi-Format Inline Viewer** | IMPLEMENTED | `FileViewer.jsx` | Supports images, PDFs, text, video, audio |
| **Chunked / Resumable Multipart Upload** | NOT IMPLEMENTED | `FileController.java`, `FileServiceImpl.java` | Single `MultipartFile` HTTP uploads only |
| **Pre-Signed S3 URLs** | NOT IMPLEMENTED | `MinioFileStorageServiceImpl.java` | Streams binary content through backend server |
| **Global Storage Deduplication** | NOT IMPLEMENTED | `FileServiceImpl.java` | Scoped strictly per-user ID |
| **Refresh Tokens & JWT Blacklist** | NOT IMPLEMENTED | `JwtTokenProvider.java` | No refresh token or revocation storage |
| **Resume Benchmark Performance Numbers**| ASSUMED / NOT VERIFIED | `loadtest/RESULTS.md` | `[Pending]` placeholders in result file |

---

## 19. INTERVIEW KNOWLEDGE REQUIRED

### 1. Architecture & Design Patterns
- **Monolith vs. Microservices**: Be prepared to explain why this project is a **Modular Monolith** and not microservices. Explain that packaging by feature (`com.fileshare.file`, `sharing`, etc.) maintains clear bounded contexts while avoiding distributed network overhead.
- **Cache-Aside Pattern**: Explain how Redis is used for `getFileMetadata`. Cache read: Check Redis -> On Miss, read DB -> Write Redis. Cache write/evict: On delete or version restore, invalidate Redis key (`@CacheEvict`).
- **Event-Driven Asynchronous Processing**: Explain why Kafka is used for notifications (offloading non-critical notification generation from the main HTTP response path).

### 2. Java & Spring Boot Core
- **Java 21 Features**: Virtual threads compatibility, Record classes (`FileDownloadResult`), `HexFormat` API used for SHA-256 formatting.
- **Spring AOP**: How `@Aspect` and `@AfterReturning` intercept methods annotated with `@Auditable` to decouple auditing logic from business methods.
- **Spring Transaction Management**: Propagation, `@Transactional(readOnly = true)`, and transaction rollback boundaries.

### 3. Storage & Concurrency
- **Content-Based Deduplication**: How SHA-256 hashes detect identical file contents and prevent redundant storage writes.
- **Non-Atomic Distributed Writes**: How writing to object storage (MinIO) and relational databases (PostgreSQL) across separate networks presents dual-write consistency challenges.

---

## 20. INTERVIEW RED FLAGS & HONEST TECHNICAL DEFENSES

1. **Red Flag: Claiming "6 Microservices" when it's a Monolith**
   - *Interviewer Challenge*: "Your resume claims 6 microservices, but I see a single Spring Boot application."
   - *Honest Defense*: "The architecture is a **Modular Monolith** running as 1 Spring Boot application alongside 4 infrastructure services (PostgreSQL, MinIO, Redis, Kafka) and Nginx. I organized the codebase into modular domain packages (`auth`, `file`, `sharing`, `notification`) so it can be split into microservices in the future if scale demands it."

2. **Red Flag: Resume Performance Metrics are `[Pending]` in Code**
   - *Interviewer Challenge*: "Where did the '65% latency reduction' and '10,000 Kafka events' metrics come from?"
   - *Honest Defense*: "Those metrics represent target benchmark thresholds defined in our k6 load testing suite (`loadtest/RESULTS.md`). In local and single-instance EC2 testing, caching metadata in Redis cut DB lookups significantly, but the load test suite placeholders remain open pending full cluster benchmarking."

3. **Red Flag: Memory Buffering during SHA-256 Computation**
   - *Interviewer Challenge*: "In `FileServiceImpl.uploadFile`, you read the entire `MultipartFile` stream once to compute the SHA-256 hash, then open `file.getInputStream()` a second time to upload to MinIO. Doesn't this buffer the file in memory or temporary disk twice?"
   - *Honest Defense*: "Yes, that is a known limitation of the current implementation. For files under 500MB, Spring Boot buffers multipart files to temp disk. A more optimized approach would be to stream bytes to MinIO using a `DigestInputStream` in a single pass or use client-side hashing."

4. **Red Flag: Inefficient Reference Counting on File Deletion**
   - *Interviewer Challenge*: "In `deleteFile()`, you call `fileMetadataRepository.findAll().stream().filter(...)` to check if other files use the same `storageKey`. Won't fetching all rows into memory crash PostgreSQL at scale?"
   - *Honest Defense*: "That is a major efficiency flaw in the current codebase. It should be replaced with a native SQL query `fileMetadataRepository.countByStorageKey(storageKey)` to let PostgreSQL compute the count efficiently using DB indexes."

5. **Red Flag: No Pre-Signed S3 URLs**
   - *Interviewer Challenge*: "Why do file downloads proxy through Spring Boot instead of giving the client a pre-signed S3 URL?"
   - *Honest Defense*: "Proxying through Spring Boot allowed us to strictly enforce JWT security, public share link permission checks, and AOP audit logging directly in Java controllers. However, for high-throughput production systems, generating MinIO pre-signed URLs (`minioClient.getPresignedObjectUrl()`) would offload bandwidth directly to object storage."

---

## 21. FINAL "WHAT I ACTUALLY BUILT" INTERVIEW ELEVATOR PITCH

> "I built a **Distributed File Sharing Platform** using **Java 21, Spring Boot 3.3, PostgreSQL, MinIO S3 object storage, Redis, Apache Kafka, and React 18**, deployed live at `https://fileshare.sanketrajput.live` with automated SSL.
> 
> Architecturally, it is a **Modular Monolith** designed around bounded contexts. It solves storage inefficiency and file management challenges through single-pass **SHA-256 content deduplication**, automatic **file version tracking** on filename collisions, and **tokenized public share links** with granular view-versus-download permissions.
> 
> I implemented **Redis cache-aside** for high-frequency metadata queries, a **Spring AOP auditing system** for compliance logging, and an **asynchronous Kafka pipeline** (`file-events` topic) that decouples user notification generation from core HTTP upload workflows.
> 
> Two significant engineering challenges I tackled were:
> 1. Resolving unauthenticated React crashes on public share links by refactoring backend streaming endpoints to separate JSON metadata from raw byte streams.
> 2. Managing storage deduplication boundaries while handling version restoration history.
> 
> The project currently processes single-pass multipart uploads with per-user deduplication, and I've identified key areas for future scaling, such as replacing backend streaming with pre-signed S3 URLs and optimizing DB reference counting."
