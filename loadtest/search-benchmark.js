import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter, Rate } from 'k6/metrics';

/**
 * ============================================================================
 * DistributedFileSystem - File Search Endpoint Load Benchmark
 * ============================================================================
 * Target Environment: Live Deployed Production (https://fileshare.sanketrajput.live)
 * 
 * PREREQUISITE:
 * Run `node loadtest/seed-5000-files.js` prior to executing this benchmark to 
 * ensure PostgreSQL contains sufficient records for realistic index performance.
 * 
 * ENDPOINT STRESSED:
 *  GET /api/v1/files/search
 * 
 * SEARCH VARIATIONS TESTED:
 *  1) Default Pagination: ?page=0&size=20
 *  2) Name Specification Search: ?name=seed_file&page=0&size=20
 *  3) Content-Type Filter Search: ?contentType=text/plain&page=0&size=20
 *  4) File Size Range Filter: ?minSize=10&maxSize=10000&page=0&size=20
 * ============================================================================
 */

// Custom Latency Metrics using native k6 res.timings.duration
const overallSearchLatency = new Trend('search_latency', true);
const paginationSearchLatency = new Trend('search_pagination_latency', true);
const nameSearchLatency = new Trend('search_name_latency', true);
const contentTypeSearchLatency = new Trend('search_type_latency', true);
const sizeRangeSearchLatency = new Trend('search_size_latency', true);

const searchFailureRate = new Rate('search_failure_rate');
const totalSearchRequests = new Counter('total_search_requests');

const BASE_URL = __ENV.BASE_URL || 'https://fileshare.sanketrajput.live';
const TEST_USER_EMAIL = __ENV.TEST_USER_EMAIL || 'seeduser@example.com';
const TEST_USER_PASSWORD = __ENV.TEST_USER_PASSWORD || 'SeedUserPass123!';

export const options = {
    stages: [
        { duration: '30s', target: Number(__ENV.TARGET_VUS || 50) },
        { duration: __ENV.SUSTAIN_DURATION || '2m', target: Number(__ENV.TARGET_VUS || 50) },
        { duration: '30s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.05'],
        search_latency: ['p(95)<1000'], // 95% of search queries complete under 1000ms
        search_name_latency: ['p(95)<1000'],
    },
};

/**
 * Setup authenticates user once to retrieve JWT token.
 */
export function setup() {
    const loginPayload = JSON.stringify({ email: TEST_USER_EMAIL, password: TEST_USER_PASSWORD });
    const headers = { 'Content-Type': 'application/json' };

    const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, loginPayload, { headers });
    if (loginRes.status === 200 && loginRes.json() && loginRes.json().data) {
        return { token: loginRes.json().data.accessToken };
    }
    return { token: null };
}

export default function (data) {
    let token = data && data.token;
    if (!token) {
        // Fallback login per VU
        const loginRes = http.post(`${BASE_URL}/api/v1/auth/login`, JSON.stringify({
            email: TEST_USER_EMAIL,
            password: TEST_USER_PASSWORD,
        }), { headers: { 'Content-Type': 'application/json' } });
        if (loginRes.status === 200 && loginRes.json().data) {
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
    // Variation 1: Default Pagination Search (?page=0&size=20)
    // ------------------------------------------------------------------------
    const res1 = http.get(`${BASE_URL}/api/v1/files/search?page=0&size=20`, { headers: authHeaders });
    totalSearchRequests.add(1);

    // Native k6 res.timings.duration
    overallSearchLatency.add(res1.timings.duration);
    paginationSearchLatency.add(res1.timings.duration);

    const check1 = check(res1, {
        'Pagination Search HTTP 200': (r) => r.status === 200,
        'Pagination Search has content array': (r) => r.json() && r.json().data && Array.isArray(r.json().data.content),
    });
    searchFailureRate.add(check1 ? 0 : 1);

    sleep(0.2);

    // ------------------------------------------------------------------------
    // Variation 2: Name Specification Search (?name=seed_file&page=0&size=20)
    // ------------------------------------------------------------------------
    const res2 = http.get(`${BASE_URL}/api/v1/files/search?name=seed_file&page=0&size=20`, { headers: authHeaders });
    totalSearchRequests.add(1);

    // Native k6 res.timings.duration
    overallSearchLatency.add(res2.timings.duration);
    nameSearchLatency.add(res2.timings.duration);

    const check2 = check(res2, {
        'Name Search HTTP 200': (r) => r.status === 200,
        'Name Search returned page': (r) => r.json() && r.json().data && r.json().data.content !== undefined,
    });
    searchFailureRate.add(check2 ? 0 : 1);

    sleep(0.2);

    // ------------------------------------------------------------------------
    // Variation 3: Content-Type Filter Search (?contentType=text/plain&page=0&size=20)
    // ------------------------------------------------------------------------
    const res3 = http.get(`${BASE_URL}/api/v1/files/search?contentType=text/plain&page=0&size=20`, { headers: authHeaders });
    totalSearchRequests.add(1);

    // Native k6 res.timings.duration
    overallSearchLatency.add(res3.timings.duration);
    contentTypeSearchLatency.add(res3.timings.duration);

    const check3 = check(res3, {
        'ContentType Search HTTP 200': (r) => r.status === 200,
    });
    searchFailureRate.add(check3 ? 0 : 1);

    sleep(0.2);

    // ------------------------------------------------------------------------
    // Variation 4: Size Range Search (?minSize=10&maxSize=10000&page=0&size=20)
    // ------------------------------------------------------------------------
    const res4 = http.get(`${BASE_URL}/api/v1/files/search?minSize=10&maxSize=10000&page=0&size=20`, { headers: authHeaders });
    totalSearchRequests.add(1);

    // Native k6 res.timings.duration
    overallSearchLatency.add(res4.timings.duration);
    sizeRangeSearchLatency.add(res4.timings.duration);

    const check4 = check(res4, {
        'Size Range Search HTTP 200': (r) => r.status === 200,
    });
    searchFailureRate.add(check4 ? 0 : 1);

    sleep(0.5);
}
