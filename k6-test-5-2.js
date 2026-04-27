import http from 'k6/http';
import { check, sleep, group } from 'k6'; // group 추가

export const options = {
    vus: 50,
    duration: '30s',
};

const BASE_URL = 'http://host.docker.internal:8090';

export default function () {
    const rand = Math.random();

    if (rand < 0.7) {
        group('Product Search', function () { // 그룹화
            const res = http.get(`${BASE_URL}/api/products/search/v1?keyword=%EB%B3%BC%EC%BA%A1`);
            check(res, {
                '검색 200 OK': (r) => r.status === 200,
                '검색 응답시간 500ms 이하': (r) => r.timings.duration < 500,
            });
        });
    } else {
        group('Product Update', function () { // 그룹화
            const res = http.put(`${BASE_URL}/api/products/1`,
                JSON.stringify({
                    name: '볼캡 수정',
                    price: 20000,
                    stock: 100,
                    description: '수정된 볼캡',
                    category: 'MERCH',
                    imageUrl: 'image.jpg'
                }),
                { headers: { 'Content-Type': 'application/json' } }
            );
            check(res, {
                '수정 200 OK': (r) => r.status === 200,
            });
        });
    }

    sleep(0.1);
}