/**
 * ============================================================================
 * DistributedFileSystem - 5,000+ File Database Seeding Script
 * ============================================================================
 * Language: Node.js (Node 18+ native fetch & FormData)
 * Target Environment: Live Production (https://fileshare.sanketrajput.live)
 * 
 * PURPOSE:
 *  Populates 5,000+ file records in PostgreSQL/MinIO to enable realistic 
 *  pagination, search filter, and list endpoint benchmarks.
 * 
 * FEATURES:
 *  - Automated authentication & user registration if needed
 *  - Controlled batch concurrency (10-20 parallel uploads)
 *  - Resumable execution (tracks progress in loadtest/.seed-progress.json)
 *  - Duplicate upload avoidance
 *  - Real-time terminal progress logger with ETA calculation
 * ============================================================================
 */

const fs = require('fs');
const path = require('path');

// Environment Configuration
const BASE_URL = process.env.BASE_URL || 'https://fileshare.sanketrajput.live';
const TEST_USER_EMAIL = process.env.SEED_USER_EMAIL || 'seeduser@example.com';
const TEST_USER_PASSWORD = process.env.SEED_USER_PASSWORD || 'SeedUserPass123!';
const TOTAL_TARGET_FILES = parseInt(process.env.TARGET_FILES || '5000', 10);
const CONCURRENCY = parseInt(process.env.CONCURRENCY || '15', 10);

const PROGRESS_FILE = path.join(__dirname, '.seed-progress.json');

// Helper to delay execution
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * Load or initialize progress state
 */
function loadProgress() {
    if (fs.existsSync(PROGRESS_FILE)) {
        try {
            const data = fs.readFileSync(PROGRESS_FILE, 'utf8');
            return JSON.parse(data);
        } catch (err) {
            console.warn('⚠️ Could not parse progress file, starting fresh.');
        }
    }
    return { uploadedCount: 0, fileIds: [] };
}

/**
 * Persist current progress state
 */
function saveProgress(progress) {
    try {
        fs.writeFileSync(PROGRESS_FILE, JSON.stringify(progress, null, 2), 'utf8');
    } catch (err) {
        console.error('❌ Failed to save progress:', err.message);
    }
}

/**
 * Authenticate or register seed user to retrieve JWT token
 */
async function authenticateUser() {
    console.log(`🔑 Authenticating seed user (${TEST_USER_EMAIL}) on ${BASE_URL}...`);

    // 1. Try Login
    try {
        const loginRes = await fetch(`${BASE_URL}/api/v1/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email: TEST_USER_EMAIL, password: TEST_USER_PASSWORD }),
        });

        if (loginRes.ok) {
            const json = await loginRes.json();
            console.log('✅ Authenticated successfully.');
            return json.data.accessToken;
        }
    } catch (e) {
        console.warn('Login request failed, attempting registration...');
    }

    // 2. Register if login failed
    console.log('📝 Registering new seed user account...');
    const regRes = await fetch(`${BASE_URL}/api/v1/auth/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: TEST_USER_EMAIL, password: TEST_USER_PASSWORD }),
    });

    if (!regRes.ok) {
        const errText = await regRes.text();
        console.error(`❌ User registration failed (${regRes.status}): ${errText}`);
        process.exit(1);
    }

    const regJson = await regRes.json();
    console.log('✅ Registered & authenticated successfully.');
    return regJson.data.accessToken;
}

/**
 * Upload a single generated file record
 */
async function uploadSingleFile(token, fileIndex) {
    const filename = `seed_file_${fileIndex}_${Date.now()}.txt`;
    const content = `Seed file content #${fileIndex} generated at ${new Date().toISOString()} for pagination benchmarking. Unique payload token: ${Math.random().toString(36).substring(2)}`;

    const formData = new FormData();
    const blob = new Blob([content], { type: 'text/plain' });
    formData.append('file', blob, filename);

    try {
        const res = await fetch(`${BASE_URL}/api/v1/files/upload`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
            },
            body: formData,
        });

        if (res.ok) {
            const json = await res.json();
            return { success: true, id: json.data.id };
        } else {
            console.warn(`⚠️ Upload HTTP ${res.status} for file #${fileIndex}`);
            return { success: false, status: res.status };
        }
    } catch (err) {
        console.error(`❌ Network error uploading file #${fileIndex}:`, err.message);
        return { success: false, error: err.message };
    }
}

/**
 * Main execution function
 */
async function main() {
    console.log('================================================================');
    console.log('   DistributedFileSystem - Production 5,000 File Seeding Tool   ');
    console.log('================================================================');
    console.log(`Target URL : ${BASE_URL}`);
    console.log(`Target Count: ${TOTAL_TARGET_FILES} files`);
    console.log(`Concurrency : ${CONCURRENCY} parallel requests`);
    console.log('----------------------------------------------------------------');

    const token = await authenticateUser();
    const progress = loadProgress();

    if (progress.uploadedCount >= TOTAL_TARGET_FILES) {
        console.log(`\n🎉 Seeding already complete! ${progress.uploadedCount} / ${TOTAL_TARGET_FILES} files seeded.`);
        console.log(`Progress file: ${PROGRESS_FILE}`);
        return;
    }

    console.log(`\n🔄 Resuming from index ${progress.uploadedCount} / ${TOTAL_TARGET_FILES}...`);

    const startTime = Date.now();
    let currentCount = progress.uploadedCount;

    while (currentCount < TOTAL_TARGET_FILES) {
        const batchSize = Math.min(CONCURRENCY, TOTAL_TARGET_FILES - currentCount);
        const batchPromises = [];

        for (let i = 0; i < batchSize; i++) {
            const fileIndex = currentCount + i + 1;
            batchPromises.push(uploadSingleFile(token, fileIndex));
        }

        const results = await Promise.all(batchPromises);

        let batchSuccessCount = 0;
        for (const res of results) {
            if (res.success) {
                batchSuccessCount++;
                progress.fileIds.push(res.id);
            }
        }

        currentCount += batchSuccessCount;
        progress.uploadedCount = currentCount;
        saveProgress(progress);

        // Progress logging & ETA calculation
        const elapsedSec = (Date.now() - startTime) / 1000;
        const rate = (currentCount - (progress.uploadedCount - batchSuccessCount)) / elapsedSec; // files/sec
        const remaining = TOTAL_TARGET_FILES - currentCount;
        const etaSec = rate > 0 ? Math.ceil(remaining / rate) : 0;
        const percentage = ((currentCount / TOTAL_TARGET_FILES) * 100).toFixed(1);

        process.stdout.write(
            `\rProgress: ${currentCount}/${TOTAL_TARGET_FILES} (${percentage}%) | ` +
            `Rate: ${rate.toFixed(1)} files/s | ETA: ${etaSec}s    `
        );

        // Gentle sleep to avoid overwhelming EC2 Nginx worker threads
        await sleep(100);
    }

    console.log(`\n\n✅ Seeding complete! ${currentCount} files successfully created.`);
    console.log(`Progress file saved to: ${PROGRESS_FILE}`);
}

main().catch((err) => {
    console.error('\n❌ Seeding failed with uncaught exception:', err);
    process.exit(1);
});
