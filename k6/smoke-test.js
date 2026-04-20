import http from 'k6/http';
import { check, sleep, group } from 'k6';

const BASE_URL = 'http://app:8090';

// 실제 존재하는 productId 넣어야 함
const PRODUCT_IDS = [1, 2, 3];
// const SEARCH_KEYWORDS = ["타잔","애니","우찬","영서","베일리"];
const SEARCH_KEYWORDS = ["볼캡","바이닐","슬로건"];

export const options = {
    vus: 5,
    duration: '30s',
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<800'],
        checks: ['rate>0.99'],
    },
};

export default function () {

    group('product list', function () {
        const res = http.get(`${BASE_URL}/api/products?page=1&size=10`);

        check(res, {
            'list status 200': (r) => r.status === 200,
            'list not empty': (r) => r.body && r.body.length > 0,
        });
    });

    group('product detail', function () {
        const productId = PRODUCT_IDS[Math.floor(Math.random() * PRODUCT_IDS.length)];
        const res = http.get(`${BASE_URL}/api/products/${productId}`);

        check(res, {
            'detail status 200': (r) => r.status === 200,
        });
    });

    group('product search', function () {
        const search_keyword = SEARCH_KEYWORDS[Math.floor(Math.random() * SEARCH_KEYWORDS.length)];
        const res = http.get(`${BASE_URL}/api/products/search?keyword=${search_keyword}`);

        check(res, {
            'search status 200': (r) => r.status === 200,
        });
    });

    group('popular products', function () {
        const res = http.get(`${BASE_URL}/api/products/popular`);

        check(res, {
            'popular status 200': (r) => r.status === 200,
        });
    });

    sleep(1);
}