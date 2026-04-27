import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    vus: 50,
    duration: '1m', // 1분간 실행
};

const BASE_URL = 'http://host.docker.internal:8090';
const START_TIME = Date.now();

export default function () {
    let productId;
    const elapsed = (Date.now() - START_TIME) / 1000; // 경과 시간(초)

    if (elapsed < 20) {
        // [구간 1] 용량 이내: 1~400번 상품만 반복 조회 (캐시 히트율 높음)
        productId = Math.floor(Math.random() * 400) + 1;
    } else if (elapsed < 40) {
        // [구간 2] 초과 직후: 1~1000번 상품을 무작위 조회 (500개 넘어가며 Eviction 발생)
        productId = Math.floor(Math.random() * 1000) + 1;
    } else {
        // [구간 3] 초과 지속: 계속해서 1~5000번 무작위 조회 (빈번한 Cache Miss 발생)
        productId = Math.floor(Math.random() * 5000) + 1;
    }

    const res = http.get(`${BASE_URL}/api/products/${productId}`);
    check(res, {
        '200 OK': (r) => r.status === 200,
        '응답시간 500ms 이하': (r) => r.timings.duration < 500,
    });
    sleep(0.01);
}