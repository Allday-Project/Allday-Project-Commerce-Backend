import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

export const options = {
    scenarios: {
        event_order_test: {
            executor: 'per-vu-iterations',
            vus: Number(__ENV.VUS || '1000'),
            iterations: 1,
            maxDuration: '3m',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.30'],

        /**
         * 전체 요청 p95
         * 성공/실패를 모두 포함한 사용자 체감 기준
         */
        order_duration_ms: ['p(95)<5000'],

        /**
         * 성공 요청 p95
         * 실제 주문에 성공한 사용자 기준 응답 시간
         */
        order_success_duration_ms: ['p(95)<5000'],

        /**
         * 실패 요청 p95
         * 품절/락 실패 등 실패 응답을 받은 사용자 기준 응답 시간
         */
        order_fail_duration_ms: ['p(95)<5000'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://app:8090';
const PRODUCT_ID = __ENV.PRODUCT_ID || '4';
const START_USER_ID = Number(__ENV.START_USER_ID || '1');
const SLOW_LIMIT_MS = Number(__ENV.SLOW_LIMIT_MS || '5000');

/**
 * Counter = "누적 카운트"
 *
 * add(1) 할 때마다 +1씩 증가
 * 최종 결과에 총합이 출력됨
 */

const orderSuccess = new Counter('order_success_count'); // 성공 주문 개수
const orderFail = new Counter('order_fail_count');       // 실패 주문 개수
const expectedFail = new Counter('order_expected_fail_count'); // 예상 가능한 실패 (품절 등)

/**
 * 성능 관련 카운트
 */
const slowRequest = new Counter('slow_request_over_5s_count'); // 5초 초과 요청 개수
const successSlowRequest = new Counter('success_slow_request_over_5s_count'); // 성공 요청 중 5초 초과 개수
const failSlowRequest = new Counter('fail_slow_request_over_5s_count'); // 실패 요청 중 5초 초과 개수

const httpConnectionFail = new Counter('http_connection_fail_count'); // 서버 연결 실패
const httpTimeout = new Counter('http_timeout_count'); // timeout 발생 개수

/**
 * 실패 원인별 카운트 (분석용)
 */
const lockFail = new Counter('lock_fail_count'); // 락 획득 실패
const userError = new Counter('user_error_count'); // 유저 관련 오류
const soldOut = new Counter('sold_out_count'); // 재고 부족
const duplicateOrder = new Counter('duplicate_order_count'); // 중복 주문
const unknownFail = new Counter('unknown_fail_count'); // 분류 안 된 실패

/**
 * Rate = "비율 계산"
 *
 * add(true) → 성공
 * add(false) → 실패
 *
 * 최종 결과:
 * order_success_rate = 성공 비율
 */
const orderSuccessRate = new Rate('order_success_rate'); // 주문 성공률

/**
 * Trend = "값의 분포 통계"
 *
 * add(숫자)로 값을 계속 넣으면
 * avg / min / max / p90 / p95 등이 계산됨
 */
const orderDuration = new Trend('order_duration_ms'); // 요청 전체 수행 시간

/**
 * 성공 요청만 따로 저장하는 Trend
 *
 * 전체 p95가 높을 때,
 * 실제 주문 성공자의 응답이 느린지 확인하기 위한 지표.
 */
const orderSuccessDuration = new Trend('order_success_duration_ms');

/**
 * 실패 요청만 따로 저장하는 Trend
 *
 * 실패 응답이 빨리 내려오는지 확인하기 위한 지표.
 * 티켓팅에서는 실패 900건이 정상일 수 있으므로 중요함.
 */
const orderFailDuration = new Trend('order_fail_duration_ms');

export default function () {
    const userId = START_USER_ID + __VU - 1;

    const url = `${BASE_URL}/api/events/products/${PRODUCT_ID}/orders?userId=${userId}`;

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
        timeout: '15s',
    };

    const startedAt = Date.now();
    const res = http.post(url, null, params);
    const durationMs = Date.now() - startedAt;

    /**
     * 전체 요청 시간 기록
     */
    orderDuration.add(durationMs);

    const body = parseJson(res.body);
    const code = body?.code || '';
    const message = body?.data?.message || body?.message || '';

    const isSuccess = res.status === 200 || res.status === 201;
    const isExpectedFail = [400, 409, 423, 429].includes(res.status);

    if (durationMs > SLOW_LIMIT_MS) {
        slowRequest.add(1);
    }

    /**
     * status=0은 서버에서 HTTP 응답을 받은 게 아님.
     * connection refused, timeout, network error 등이 여기에 해당.
     */
    if (res.status === 0) {
        const error = String(res.error || '').toLowerCase();

        /**
         * 서버 응답을 못 받은 요청도 실패 요청 시간으로 기록한다.
         * timeout이 p95를 얼마나 끌어올리는지 확인 가능.
         */
        orderFailDuration.add(durationMs);

        if (durationMs > SLOW_LIMIT_MS) {
            failSlowRequest.add(1);
        }

        if (error.includes('connection refused')) {
            httpConnectionFail.add(1);
        } else if (error.includes('timeout')) {
            httpTimeout.add(1);
        } else {
            unknownFail.add(1);
        }

        orderFail.add(1);
        orderSuccessRate.add(false);
        return;
    }

    if (isSuccess) {
        orderSuccess.add(1);
        orderSuccessRate.add(true);

        /**
         * 성공 요청만 따로 p95를 계산하기 위해 기록
         */
        orderSuccessDuration.add(durationMs);

        if (durationMs > SLOW_LIMIT_MS) {
            successSlowRequest.add(1);
        }
    } else {
        orderFail.add(1);
        orderSuccessRate.add(false);

        /**
         * 실패 요청만 따로 p95를 계산하기 위해 기록
         */
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
        '성공 또는 예상 가능한 실패': () => isSuccess || isExpectedFail,
        '500번대 서버 에러 없음': () => res.status < 500,
        'HTTP 연결 실패 아님': () => res.status !== 0,
        '5초 이하 응답': () => durationMs <= SLOW_LIMIT_MS,
    });
}

function classifyError(code, message) {
    if (code === 'L001') {
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
        message.toLowerCase().includes('sold')
    ) {
        soldOut.add(1);
        return;
    }

    if (
        message.includes('중복') ||
        message.includes('이미') ||
        message.toLowerCase().includes('duplicate')
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