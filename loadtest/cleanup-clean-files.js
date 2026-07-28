/**
 * ============================================================================
 * DistributedFileSystem - Production Load Test Artifact Cleanup Tool
 * ============================================================================
 * Language: Node.js (Node 18+ native fetch)
 * Target Environment: Live Production (https://fileshare.sanketrajput.live)
 * 
 * PURPOSE:
 *  Scrubs production PostgreSQL and MinIO of load-test generated files 
 *  (e.g., matching prefix 'seed_file_', 'loadtest_', or 'event_') to restore 
 *  clean environment state after running benchmark tests.
 * ============================================================================
 */

const BASE_URL = process.env.BASE_URL || 'https://fileshare.sanketrajput.live';
const CONCURRENCY = parseInt(process.env.CONCURRENCY || '15', 10);

// Array of test accounts used during load testing
const TEST_ACCOUNTS = [
    { email: process.env.TEST_USER_EMAIL || 'loadtest@example.com', password: process.env.TEST_USER_PASSWORD || 'LoadTestPass123!' },
    { email: process.env.SEED_USER_EMAIL || 'seeduser@example.com', password: process.env.SEED_USER_PASSWORD || 'SeedUserPass123!' },
    { email: process.env.KAFKA_USER_EMAIL || 'kafkaburst@example.com', password: process.env.KAFKA_USER_PASSWORD || 'KafkaBurstPass123!' },
];

const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

/**
 * Authenticate single test user and retrieve JWT token
 */
async function authenticate(email, password) {
    try {
        const res = await fetch(`${BASE_URL}/api/v1/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
        });
        if (res.ok) {
            const json = await res.json();
            return json.data.accessToken;
        }
    } catch (e) {
        // User may not exist if test wasn't run for this user
    }
    return null;
}

/**
 * Fetch all files owned by authenticated user
 */
async function getUserFiles(token) {
    try {
        const res = await fetch(`${BASE_URL}/api/v1/files`, {
            method: 'GET',
            headers: { 'Authorization': `Bearer ${token}` },
        });
        if (res.ok) {
            const json = await res.json();
            return Array.isArray(json.data) ? json.data : [];
        }
    } catch (err) {
        console.error('❌ Failed to fetch user files:', err.message);
    }
    return [];
}

/**
 * Delete a single file by ID
 */
async function deleteFile(token, fileId) {
    try {
        const res = await fetch(`${BASE_URL}/api/v1/files/${fileId}`, {
            method: 'DELETE',
            headers: { 'Authorization': `Bearer ${token}` },
        });
        return res.ok;
    } catch (err) {
        return false;
    }
}

/**
 * Process cleanup for a specific user account
 */
async function cleanupUser(account) {
    console.log(`\n🧹 Processing cleanup for account: ${account.email}...`);
    const token = await authenticate(account.email, account.password);

    if (!token) {
        console.log(`⚠️ User ${account.email} not found or unable to login. Skipping.`);
        return;
    }

    const files = await getUserFiles(token);
    if (files.length === 0) {
        console.log(`✅ No files found for ${account.email}.`);
        return;
    }

    console.log(`Found ${files.length} total files. Identifying load-test generated files...`);

    // Filter test files
    const testFiles = files.filter((f) => {
        const name = (f.name || '').toLowerCase();
        return name.startsWith('seed_file_') || name.startsWith('loadtest_') || name.startsWith('event_');
    });

    console.log(`Targeting ${testFiles.length} test files for deletion.`);
    if (testFiles.length === 0) return;

    let deletedCount = 0;
    for (let i = 0; i < testFiles.length; i += CONCURRENCY) {
        const batch = testFiles.slice(i, i + CONCURRENCY);
        const results = await Promise.all(batch.map((file) => deleteFile(token, file.id)));

        deletedCount += results.filter(Boolean).length;
        process.stdout.write(`\rDeleted ${deletedCount} / ${testFiles.length} files...`);
        await sleep(50);
    }

    console.log(`\n✅ Successfully cleaned ${deletedCount} test files for ${account.email}.`);
}

async function main() {
    console.log('================================================================');
    console.log('    DistributedFileSystem - Production Load Test Artifact Cleaner ');
    console.log('================================================================');
    console.log(`Target URL: ${BASE_URL}\n`);

    for (const account of TEST_ACCOUNTS) {
        await cleanupUser(account);
    }

    console.log('\n================================================================');
    console.log('🎉 Cleanup process finished.');
    console.log('================================================================');
}

main().catch((err) => {
    console.error('❌ Cleanup failed:', err);
    process.exit(1);
});
