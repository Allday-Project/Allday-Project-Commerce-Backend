import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 10 },
        { duration: '1m', target: 50 },
        { duration: '30s', target: 100 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
    ],
};

const BASE_URL = 'http://host.docker.internal:8090';

export default function () {
    const res = http.get(`${BASE_URL}/api/products/search/v1?keyword=%EB%B3%BC%EC%BA%A1`);

    check(res, {
        '200 OK': (r) => r.status === 200,
        '응답시간 500ms 이하': (r) => r.timings.duration < 500,
    });

    sleep(1);
}