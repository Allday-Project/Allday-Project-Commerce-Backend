import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 100 }, // 30초 동안 100명까지 증가
        { duration: '30s', target: 200 }, // 30초 동안 200명까지 증가
        { duration: '30s', target: 300 }, // 30초 동안 300명까지 증가
        { duration: '30s', target: 0 },   // 30초 동안 감소
    ],
    thresholds: {
        'http_req_failed': ['rate<0.01'],
    },
};

const version = __ENV.VERSION || 'v2';

export default function () {
    // 1-1 테스트와 동일하게 'apple' 키워드 사용
    const url = `http://host.docker.internal:8090/api/products/search/${version}?keyword=apple`;
    const res = http.get(url);
    check(res, { 'is status 200': (r) => r.status === 200 });
    sleep(0.1);
}