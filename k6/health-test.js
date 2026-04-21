import http from 'k6/http';
import { check } from 'k6';

export const options = {
    vus: 1,
    iterations: 5,
    thresholds: {
        http_req_failed: ['rate==0'],
        http_req_duration: ['p(95)<500'],
        checks: ['rate==1'],
    },
};

const BASE_URL = 'http://app:8090';

export default function () {
    const res = http.get(`${BASE_URL}/actuator/health`);

    check(res, {
        'status 200': (r) => r.status === 200,
        'service UP': (r) => r.body.includes('UP'),
    });
}