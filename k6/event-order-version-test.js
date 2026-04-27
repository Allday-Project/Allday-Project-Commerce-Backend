import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

export const options = {
    scenarios: {
        event_order_test: {
            executor: 'per-vu-iterations',
            vus: Number(__ENV.VUS || '100'),
            iterations: 1,
            maxDuration: '3m',
        },
    },
    summaryTrendStats: ['min', 'med', 'avg', 'p(90)', 'p(95)', 'p(99)', 'max'],
    thresholds: {
        order_duration_ms: ['p(95)<5000'],
        order_success_duration_ms: ['p(95)<5000'],
        order_fail_duration_ms: ['p(95)<5000'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://app:8090';
const VERSION = __ENV.VERSION || 'v1';
const PRODUCT_ID = __ENV.PRODUCT_ID || '4';
const START_USER_ID = Number(__ENV.START_USER_ID || '1');
const SLOW_LIMIT_MS = Number(__ENV.SLOW_LIMIT_MS || '5000');

const orderSuccess = new Counter('order_success_count');
const orderFail = new Counter('order_fail_count');
const expectedFail = new Counter('order_expected_fail_count');

const lockFail = new Counter('lock_fail_count');
const userError = new Counter('user_error_count');
const soldOut = new Counter('sold_out_count');
const duplicateOrder = new Counter('duplicate_order_count');
const unknownFail = new Counter('unknown_fail_count');

const slowRequest = new Counter('slow_request_over_5s_count');
const successSlowRequest = new Counter('success_slow_request_over_5s_count');
const failSlowRequest = new Counter('fail_slow_request_over_5s_count');

const httpConnectionFail = new Counter('http_connection_fail_count');
const httpTimeout = new Counter('http_timeout_count');

const orderSuccessRate = new Rate('order_success_rate');

const orderDuration = new Trend('order_duration_ms', true);
const orderSuccessDuration = new Trend('order_success_duration_ms', true);
const orderFailDuration = new Trend('order_fail_duration_ms', true);

export default function () {
    const userId = START_USER_ID + __VU - 1;

    const url = `${BASE_URL}/api/events/${VERSION}/products/${PRODUCT_ID}/orders?userId=${userId}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: '20s',
        tags: {
            version: VERSION,
        },
    };

    const startedAt = Date.now();
    const res = http.post(url, null, params);
    const durationMs = Date.now() - startedAt;

    orderDuration.add(durationMs);

    const body = parseJson(res.body);
    const code = body?.code || '';
    const message = body?.data?.message || body?.message || '';

    const isSuccess = res.status === 200 || res.status === 201;
    const isExpectedFail = [400, 409, 423, 429].includes(res.status);

    if (durationMs > SLOW_LIMIT_MS) {
        slowRequest.add(1);
    }

    if (res.status === 0) {
        orderFail.add(1);
        orderSuccessRate.add(false);
        orderFailDuration.add(durationMs);

        if (durationMs > SLOW_LIMIT_MS) {
            failSlowRequest.add(1);
        }

        const error = String(res.error || '').toLowerCase();

        if (error.includes('connection refused')) {
            httpConnectionFail.add(1);
        } else if (error.includes('timeout')) {
            httpTimeout.add(1);
        } else {
            unknownFail.add(1);
        }

        return;
    }

    if (isSuccess) {
        orderSuccess.add(1);
        orderSuccessRate.add(true);
        orderSuccessDuration.add(durationMs);

        if (durationMs > SLOW_LIMIT_MS) {
            successSlowRequest.add(1);
        }
    } else {
        orderFail.add(1);
        orderSuccessRate.add(false);
        orderFailDuration.add(durationMs);

        if (durationMs > SLOW_LIMIT_MS) {
            failSlowRequest.add(1);
        }

        if (isExpectedFail) {
            expectedFail.add(1);
        }

        classifyError(code, message);
    }

    check(res, {
        [`${VERSION} 성공 또는 예상 가능한 실패`]: () => isSuccess || isExpectedFail,
        [`${VERSION} 500번대 서버 에러 없음`]: () => res.status < 500,
        [`${VERSION} HTTP 연결 실패 아님`]: () => res.status !== 0,
        [`${VERSION} ${SLOW_LIMIT_MS}ms 이하 응답`]: () => durationMs <= SLOW_LIMIT_MS,
    });
}

function classifyError(code, message) {
    const lowerMessage = String(message || '').toLowerCase();

    if (code === 'L001' || message.includes('락') || lowerMessage.includes('lock')) {
        lockFail.add(1);
        return;
    }

    if (code.startsWith('U')) {
        userError.add(1);
        return;
    }

    if (
        message.includes('재고') ||
        message.includes('품절') ||
        lowerMessage.includes('sold') ||
        lowerMessage.includes('stock')
    ) {
        soldOut.add(1);
        return;
    }

    if (
        message.includes('중복') ||
        message.includes('이미') ||
        lowerMessage.includes('duplicate') ||
        lowerMessage.includes('already')
    ) {
        duplicateOrder.add(1);
        return;
    }

    unknownFail.add(1);
}

function parseJson(body) {
    try {
        return JSON.parse(body);
    } catch (e) {
        return null;
    }
}