import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Trend, Rate } from 'k6/metrics';

/**
 * ============================================================================
 * DistributedFileSystem - Kafka Event Burst Load Benchmark
 * ============================================================================
 * Target Environment: Live Deployed Production (https://fileshare.sanketrajput.live)
 * 
 * VERIFIED ENDPOINTS STRESSED:
 *  1) POST /api/v1/files/upload -> Triggers FILE_UPLOADED Kafka event
 *  2) POST /api/v1/files/{fileId}/share -> Triggers FILE_SHARED Kafka event
 *     Request Payload: { "permission": "DOWNLOAD" }
 * 
 * GOAL:
 *  Generate at least 10,000 Kafka events (Upload + Share) during the benchmark run.
 * 
 * LATENCY MEASUREMENTS:
 *  Uses native k6 `response.timings.duration` for exact sub-millisecond precision.
 * 
 * IMPORTANT VERIFICATION NOTE:
 *  Kafka event production is measured on the HTTP producer side by counting 
 *  successful 201/200 API responses.
 * 
 *  CONSUMER THROUGHPUT MUST BE VERIFIED INDEPENDENTLY USING:
 *   - Prometheus metrics (kafka_events_published_total, consumer lag)
 *   - Grafana dashboards
 *   - Kafka consumer group counters
 *   - Database notification row counts
 * 
 *  DO NOT FABRICATE OR INVENT CONSUMER THROUGHPUT NUMBERS IN REPORTS.
 * ============================================================================
 */

const BASE_URL = __ENV.BASE_URL || 'https://fileshare.sanketrajput.live';
const TEST_USER_EMAIL = __ENV.TEST_USER_EMAIL || 'kafkaburst@example.com';
const TEST_USER_PASSWORD = __ENV.TEST_USER_PASSWORD || 'KafkaBurstPass123!';

// Custom Latency & Throughput Metrics using native k6 res.timings.duration
const uploadLatency = new Trend('kafka_upload_latency', true);
const shareLatency = new Trend('kafka_share_latency', true);
const totalHttpRequests = new Counter('total_http_requests');
const estimatedKafkaEventsProduced = new Counter('estimated_kafka_events_produced');
const requestFailureRate = new Rate('request_failure_rate');

export const options = {
    stages: [
        { duration: '20s', target: Number(__ENV.TARGET_VUS || 50) },
        { duration: __ENV.SUSTAIN_DURATION || '3m', target: Number(__ENV.TARGET_VUS || 50) },
        { duration: '20s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],
        estimated_kafka_events_produced: ['count>=1000'],
    },
};

/**
 * Setup runs once before execution to register and authenticate test user.
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
    if (loginRes.status === 200 && loginRes.json() && loginRes.json().data) {
        return { token: loginRes.json().data.accessToken };
    }
    return { token: null };
}

export default function (data) {
    let token = data && data.token;
    if (!token) {
        const loginPayload = JSON.stringify({ email: TEST_USER_EMAIL, password: TEST_USER_PASSWORD });
        const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, {
            headers: { 'Content-Type': 'application/json' },
        });
        if (loginRes.status === 200 && loginRes.json() && loginRes.json().data) {
            token = loginRes.json().data.accessToken;
        } else {
            sleep(1);
            return;
        }
    }

    const authHeaders = {
        'Authorization': `Bearer ${token}`,
    };

    // ------------------------------------------------------------------------
    // Event 1: Upload File (Triggers FILE_UPLOADED Kafka Event)
    // ------------------------------------------------------------------------
    const fileContent = `Kafka Burst Test File - VU ${__VU} - Iter ${__ITER} - ${Date.now()}`;
    const filePayload = {
        file: http.file(fileContent, `event_${__VU}_${Date.now()}.txt`, 'text/plain'),
    };

    const uploadRes = http.post(`${BASE_URL}/api/v1/files/upload`, filePayload, {
        headers: authHeaders,
    });
    totalHttpRequests.add(1);

    // Native k6 response.timings.duration
    uploadLatency.add(uploadRes.timings.duration);

    const uploadSuccess = check(uploadRes, {
        'Kafka Event Upload HTTP 201': (r) => r.status === 201,
        'Upload returned valid ID': (r) => r.json() && r.json().data && r.json().data.id !== undefined,
    });

    if (uploadSuccess) {
        estimatedKafkaEventsProduced.add(1); // 1 FILE_UPLOADED event
        requestFailureRate.add(0);
        const fileId = uploadRes.json().data.id;

        // --------------------------------------------------------------------
        // Event 2: Share File (Triggers FILE_SHARED Kafka Event)
        // Verified Endpoint Payload: { "permission": "DOWNLOAD" }
        // --------------------------------------------------------------------
        const sharePayload = JSON.stringify({
            permission: 'DOWNLOAD',
        });

        const shareRes = http.post(`${BASE_URL}/api/v1/files/${fileId}/share`, sharePayload, {
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json',
            },
        });
        totalHttpRequests.add(1);

        // Native k6 response.timings.duration
        shareLatency.add(shareRes.timings.duration);

        const shareSuccess = check(shareRes, {
            'Kafka Event Share HTTP 201': (r) => r.status === 201,
        });

        if (shareSuccess) {
            estimatedKafkaEventsProduced.add(1); // 1 FILE_SHARED event
            requestFailureRate.add(0);
        } else {
            requestFailureRate.add(1);
        }
    } else {
        requestFailureRate.add(1);
    }

    // Delay to sustain steady event burst throughput
    sleep(0.05);
}
