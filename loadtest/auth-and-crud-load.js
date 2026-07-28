import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter, Rate } from 'k6/metrics';

/**
 * ============================================================================
 * DistributedFileSystem - Authentication & CRUD Load Benchmark
 * ============================================================================
 * Target Environment: Live Deployed Production (https://fileshare.sanketrajput.live)
 * 
 * RECOMMENDED BENCHMARK ESCALATION ORDER:
 * First run with smaller VU targets to avoid overwhelming the AWS EC2 instance:
 *   1) 10 VUs
 *   2) 25 VUs
 *   3) 50 VUs
 *   4) 100 VUs
 *   5) 200 VUs
 *   6) 300 VUs (Full Target)
 * 
 * ABORT ESCALATION IF:
 *  - HTTP failures > 5%
 *  - JVM memory exhaustion
 *  - PostgreSQL connection pool saturation
 *  - Kafka backlog grows excessively
 * ============================================================================
 */

// Custom Latency Metrics using native k6 res.timings.duration
const loginLatency = new Trend('login_latency', true);
const uploadLatency = new Trend('upload_latency', true);
const listLatency = new Trend('list_latency', true);
const metadataGetLatency = new Trend('metadata_get_latency', true);
const cacheMissLatency = new Trend('cache_miss_latency', true);
const cacheHitLatency = new Trend('cache_hit_latency', true);
const deleteLatency = new Trend('delete_latency', true);
const iterationDurationMetric = new Trend('iteration_duration', true);

// Throughput and Reliability Metrics
const uploadThroughputBytes = new Counter('upload_throughput_bytes');
const successfulRequests = new Counter('successful_requests');
const failedRequests = new Counter('failed_requests');
const requestFailureRate = new Rate('request_failure_rate');

// Configurable Options
const BASE_URL = __ENV.BASE_URL || 'https://fileshare.sanketrajput.live';
const TEST_USER_EMAIL = __ENV.TEST_USER_EMAIL || 'loadtest@example.com';
const TEST_USER_PASSWORD = __ENV.TEST_USER_PASSWORD || 'LoadTestPass123!';

export const options = {
    // Target staged execution: 30s ramp-up -> sustain load -> 30s ramp-down
    stages: [
        { duration: '30s', target: Number(__ENV.TARGET_VUS || 300) }, // Ramp-up
        { duration: __ENV.SUSTAIN_DURATION || '2m', target: Number(__ENV.TARGET_VUS || 300) }, // Sustain
        { duration: '30s', target: 0 }, // Ramp-down
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'], // Failure rate < 5%
        http_req_duration: ['p(95)<2000'], // 95% requests completed under 2000ms
        request_failure_rate: ['rate<0.05'],
        login_latency: ['p(95)<1500'],
        upload_latency: ['p(95)<3000'],
        cache_hit_latency: ['p(95)<300'],
    },
};

/**
 * Setup function runs once before test execution.
 * Ensures test user exists on production and returns initial JWT.
 */
export function setup() {
    const registerPayload = JSON.stringify({
        email: TEST_USER_EMAIL,
        password: TEST_USER_PASSWORD,
    });
    const headers = { 'Content-Type': 'application/json' };

    // Register user if not existing
    http.post(`${BASE_URL}/api/v1/auth/register`, registerPayload, { headers });

    // Authenticate user
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, registerPayload, { headers });
    if (loginRes.status === 200) {
        const body = loginRes.json();
        return { token: body && body.data && body.data.accessToken };
    }
    return { token: null };
}

/**
 * Main Virtual User iteration loop representing authentic user activity.
 */
export default function (data) {
    const iterStartTime = Date.now();
    const headers = { 'Content-Type': 'application/json' };

    // ------------------------------------------------------------------------
    // Step 1: Login Authentication
    // ------------------------------------------------------------------------
    const loginPayload = JSON.stringify({
        email: TEST_USER_EMAIL,
        password: TEST_USER_PASSWORD,
    });
    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, { headers });

    // Use native k6 response.timings.duration
    loginLatency.add(loginRes.timings.duration);

    const loginSuccess = check(loginRes, {
        'Login status is 200': (r) => r.status === 200,
        'Login returned access token': (r) => r.json() && r.json().data && r.json().data.accessToken !== undefined,
    });

    if (!loginSuccess) {
        failedRequests.add(1);
        requestFailureRate.add(1);
        sleep(1);
        return;
    }

    successfulRequests.add(1);
    requestFailureRate.add(0);
    const authToken = loginRes.json().data.accessToken;
    const authHeaders = {
        'Authorization': `Bearer ${authToken}`,
    };

    // ------------------------------------------------------------------------
    // Step 2: Upload Small Multipart File
    // ------------------------------------------------------------------------
    const sampleContent = `Load test payload - VU ${__VU} - Iteration ${__ITER} - ${Date.now()}`;
    const contentBytes = sampleContent.length;
    const filePayload = {
        file: http.file(sampleContent, `loadtest_${__VU}_${Date.now()}.txt`, 'text/plain'),
    };

    const uploadRes = http.post(`${BASE_URL}/api/v1/files/upload`, filePayload, {
        headers: authHeaders,
    });

    // Use native k6 response.timings.duration
    uploadLatency.add(uploadRes.timings.duration);

    const uploadSuccess = check(uploadRes, {
        'Upload status is 201': (r) => r.status === 201,
        'Upload returned valid file ID': (r) => r.json() && r.json().data && r.json().data.id !== undefined,
    });

    let uploadedFileId = null;
    if (uploadSuccess) {
        successfulRequests.add(1);
        requestFailureRate.add(0);
        uploadThroughputBytes.add(contentBytes);
        uploadedFileId = uploadRes.json().data.id;
    } else {
        failedRequests.add(1);
        requestFailureRate.add(1);
    }

    // ------------------------------------------------------------------------
    // Step 3: List Files
    // ------------------------------------------------------------------------
    const listRes = http.get(`${BASE_URL}/api/v1/files`, { headers: authHeaders });

    // Use native k6 response.timings.duration
    listLatency.add(listRes.timings.duration);

    const listSuccess = check(listRes, {
        'List files status is 200': (r) => r.status === 200,
        'List response contains data array': (r) => r.json() && Array.isArray(r.json().data),
    });

    if (listSuccess) {
        successfulRequests.add(1);
        requestFailureRate.add(0);
    } else {
        failedRequests.add(1);
        requestFailureRate.add(1);
    }

    // If upload succeeded, perform metadata cache tests and cleanup
    if (uploadedFileId) {
        // --------------------------------------------------------------------
        // Step 4: First GET Metadata (Cold Cache / Cache Miss)
        // --------------------------------------------------------------------
        const missRes = http.get(`${BASE_URL}/api/v1/files/${uploadedFileId}`, { headers: authHeaders });

        // Use native k6 response.timings.duration
        metadataGetLatency.add(missRes.timings.duration);
        cacheMissLatency.add(missRes.timings.duration);

        const missSuccess = check(missRes, {
            'Cache miss GET status is 200': (r) => r.status === 200,
            'Cache miss metadata returned': (r) => r.json() && r.json().data && r.json().data.id === uploadedFileId,
        });

        if (missSuccess) {
            successfulRequests.add(1);
            requestFailureRate.add(0);
        } else {
            failedRequests.add(1);
            requestFailureRate.add(1);
        }

        sleep(0.1);

        // --------------------------------------------------------------------
        // Step 5: Second GET Metadata (Warm Cache / Cache Hit)
        // --------------------------------------------------------------------
        const hitRes = http.get(`${BASE_URL}/api/v1/files/${uploadedFileId}`, { headers: authHeaders });

        // Use native k6 response.timings.duration
        metadataGetLatency.add(hitRes.timings.duration);
        cacheHitLatency.add(hitRes.timings.duration);

        const hitSuccess = check(hitRes, {
            'Cache hit GET status is 200': (r) => r.status === 200,
            'Cache hit metadata returned': (r) => r.json() && r.json().data && r.json().data.id === uploadedFileId,
        });

        if (hitSuccess) {
            successfulRequests.add(1);
            requestFailureRate.add(0);
        } else {
            failedRequests.add(1);
            requestFailureRate.add(1);
        }

        sleep(0.1);

        // --------------------------------------------------------------------
        // Step 6: Delete Uploaded File (Per-Iteration Cleanup)
        // --------------------------------------------------------------------
        const deleteRes = http.del(`${BASE_URL}/api/v1/files/${uploadedFileId}`, null, { headers: authHeaders });

        // Use native k6 response.timings.duration
        deleteLatency.add(deleteRes.timings.duration);

        const deleteSuccess = check(deleteRes, {
            'Delete file status is 200': (r) => r.status === 200,
        });

        if (deleteSuccess) {
            successfulRequests.add(1);
            requestFailureRate.add(0);
        } else {
            failedRequests.add(1);
            requestFailureRate.add(1);
        }
    }

    // Capture total iteration duration
    iterationDurationMetric.add(Date.now() - iterStartTime);

    // Pacing pause before next iteration
    sleep(1);
}
