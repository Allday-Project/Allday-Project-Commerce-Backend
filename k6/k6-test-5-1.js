import http from 'k6/http';
import { check, sleep, group } from 'k6';

export const options = {
    vus: 50,
    duration: '30s',
};

const BASE_URL = 'http://host.docker.internal:8090';

export default function () {
    if (Math.random() < 0.5) {
        group('Product Search', () => {
            const res = http.get(`${BASE_URL}/api/products/search/v1?keyword=%EB%B3%BC%EC%BA%A1`);
            check(res, {
                '검색 200 OK': (r) => r.status === 200,
                '검색 응답시간 500ms 이하': (r) => r.timings.duration < 500,
            });
        });
    } else {
        group('Product Detail', () => {
            const res = http.get(`${BASE_URL}/api/products/1`);
            check(res, {
                '조회 200 OK': (r) => r.status === 200,
                '단건조회 응답시간 500ms 이하': (r) => r.timings.duration < 500,
            });
        });
    }

    sleep(0.1);
}