# Systematic QA & Bug Fix Report — Distributed File Sharing Platform

This document details the comprehensive QA pass, root-cause bug fixes, and feature additions across all functional areas of the Distributed File Sharing Platform.

---

## 🐞 Critical Bug Report: Unauthenticated White Screen Bug (PART A)

- **Severity**: Critical (Launch Blocking)
- **Symptom**: When a public share link (`/share/{token}`) is opened in a separate, unauthenticated browser context (e.g. Incognito session without a JWT token), the page rendered a blank white screen instead of displaying the shared file metadata or preview.
- **Empirical Root Cause Diagnosis**:
  1. **Backend Query Parameter Mismatch (Primary Cause)**: In `PublicShareController.java`, the endpoint `@GetMapping("/{token}")` defined `@RequestParam(value = "download", defaultValue = "true") boolean download`. When `PublicSharePage.jsx` called `shareApi.getShareByToken(token)` without `?download=false`, the backend hit the `download=true` branch by default and returned binary file stream bytes (`byte[]`) instead of the `ShareResponseDto` JSON metadata object.
  2. **Frontend Uncaught Render TypeError (Secondary Cause)**: Because `res.data` was a raw string/ArrayBuffer instead of a JSON object `{ file: { originalFilename, contentType, sizeBytes } }`, `shareData.contentType` was `undefined`. In `PublicSharePage.jsx`, dereferencing properties without optional chaining (`shareData.contentType` vs `shareData?.file?.contentType`) threw an uncaught React `TypeError` during rendering, which crashed the React component tree into a **blank white screen**.
  3. **Stale/Malformed Authorization Header**: In `client.js`, `localStorage.getItem('token')` string `"null"` or `"undefined"` was being attached as `Authorization: Bearer undefined` to public API requests, triggering unnecessary 401 response interceptor rejects.

- **Fix Applied**:
  1. **Backend**: Refactored `PublicShareController.java` to separate metadata and streaming endpoints cleanly:
     - `GET /api/v1/share/{token}`: Always returns `ApiResponse<ShareResponseDto>` JSON metadata.
     - `GET /api/v1/share/{token}/stream`: Streams byte contents with query parameter `inline` (`true` for inline viewing, `false` for attachment download).
  2. **Backend Permission Enforcement**: Updated `SharingServiceImpl.streamSharedFile` to verify:
     - If `share.permission == VIEW` and `inline == false` (download attempted), backend throws `AccessDeniedException` (HTTP 403: *"Downloading is disabled for this view-only share link"*).
  3. **Frontend**: Refactored `PublicSharePage.jsx` and [shareApi.js](file:///d:/3rd/ace/sem%207/project/DisfileSys/frontend/src/api/shareApi.js) to safely extract nested `shareData?.file` properties and render state-aware UI alerts for loading, invalid token (404), expired share (403), and revoked share (403).

- **Verification Conducted**:
  - Opened generated share links in a fresh Incognito window without JWT headers.
  - Confirmed metadata loads, permission badges display, and zero white screen crashes or console errors occur.
- **Status**: ✅ **ROOT CAUSE FIXED & VERIFIED**

---

## 🎨 New Feature: Multi-Format Inline File Viewer (PART B)

Built a modern, responsive, format-aware [FileViewer.jsx](file:///d:/3rd/ace/sem%207/project/DisfileSys/frontend/src/components/file/FileViewer.jsx) component supporting inline viewing and streaming for:

1. **Images** (`image/jpeg`, `image/png`, `image/gif`, `image/webp`, `image/svg+xml`):
   - Renders directly via `<img>` tag pointing to stream URL (`inline=true`).
   - Features zoom-in/fit toggle control, shadow cards, and image load skeletons.
2. **PDF Documents** (`application/pdf`):
   - Renders inline using browser native `<iframe>` pointing to stream URL (`inline=true`).
3. **Plain Text & Code Files** (`.txt`, `.md`, `.json`, `.csv`, `.log`, `.html`, `.js`, `.py`, `.java`):
   - Fetches content via `fetch(streamUrl)` and renders in a scrollable monospace `<pre>` block with copy-code action.
4. **Video Files** (`video/mp4`, `video/webm`, `video/ogg`, `video/quicktime`):
   - Renders via `<video controls controlsList="nodownload">` pointing directly to backend stream URL.
5. **Audio Files** (`audio/mp3`, `audio/wav`, `audio/ogg`, `audio/mpeg`):
   - Renders via `<audio controls>` with audio card waveform artwork.
6. **Office Documents & Binary Files** (`.docx`, `.xlsx`, `.pptx`, `.zip`):
   - Graceful fallback card displaying: *"Preview Not Available"* with content type, size, and prominent Download button (if permitted).

---

## 🧪 Comprehensive Verification Matrix

| Category | Test Case | Environment | Expected Result | Actual Result | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **White Screen Bug** | Open `/share/{token}` unauthenticated | Incognito Window | Shows metadata & preview | Metadata & preview render cleanly | ✅ PASS |
| **Image Preview** | `PNG`/`JPG` file (VIEW perm) | Incognito Window | Inline image with zoom | Inline image renders cleanly | ✅ PASS |
| **PDF Preview** | `PDF` document (VIEW perm) | Incognito Window | Inline PDF iframe viewer | Native browser PDF frame renders | ✅ PASS |
| **Text/Code Preview** | `TXT`/`JSON`/`JS` file (VIEW perm) | Incognito Window | Monospace `<pre>` text box | Scrollable code box renders | ✅ PASS |
| **Video Streaming** | `MP4` video (DOWNLOAD perm) | Incognito Window | Video player + download | Native video player & download work | ✅ PASS |
| **Audio Streaming** | `MP3` audio (DOWNLOAD perm) | Incognito Window | Audio player + download | Audio player & download work | ✅ PASS |
| **VIEW Enforcement** | Direct download attempt on VIEW share | Backend Curl / API | HTTP 403 Forbidden | HTTP 403 *"Downloading is disabled..."* | ✅ PASS |
| **Expired Link** | Share link past `expiresAt` | Incognito Window | HTTP 403 Red Alert | Red card *"Access Restricted"* | ✅ PASS |
| **Revoked Link** | Revoked share link | Incognito Window | HTTP 403 Red Alert | Red card *"This share link has been revoked"* | ✅ PASS |
| **Invalid Token** | Non-existent token string | Incognito Window | HTTP 404 Red Alert | Red card *"Shared Link token not found"* | ✅ PASS |
| **Share Token Stability** | Re-open share modal for file | Logged-in User | Token remains identical | Reuses active share token | ✅ PASS |
| **Clipboard Copy** | Click "Copy Link" on HTTP host | Non-Secure Context | Uses DOM execCommand fallback | Toast *"Link copied to clipboard!"* | ✅ PASS |
| **Dashboard Preview** | Click "Preview" on file card | Logged-in Dashboard | Preview modal overlay | Preview modal renders FileViewer | ✅ PASS |
