import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = 'http://app:8090';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 10 },
        { duration: '30s', target: 20 },
        { duration: '1m', target: 20 },
        { duration: '20s', target: 0 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
    },
};

export default function () {
    const res = http.get(`${BASE_URL}/api/products?page=0&size=10`);

    check(res, {
        'status 200': (r) => r.status === 200,
    });

    sleep(1);
}