import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 30, // 아까 안정적이었던 30명으로 진행
    duration: '30s',
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    thresholds: {
        'http_req_failed': ['rate<0.01'],
    },
};

const version = __ENV.VERSION || 'v1';

export default function () {
    // DB에 절대 없을 법한 고정 키워드 사용
    const keyword = `ghost_product_zero_stock_99999`;
    const encodedKeyword = encodeURIComponent(keyword);

    const url = `http://host.docker.internal:8090/api/products/search/${version}?keyword=${encodedKeyword}`;

    const params = { tags: { version: version } };
    const res = http.get(url, params);

    check(res, {
        'is status 200': (r) => r.status === 200,
        // 결과가 비어있는지 확인 (정상적인 동작)
        'is empty result': (r) => {
            try {
                const body = JSON.parse(r.body);
                return Array.isArray(body.content) && body.content.length === 0;
            } catch (e) {
                return false;
            }
        },
    });

    sleep(0.1);
}