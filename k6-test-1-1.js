import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    // 50명 고정 부하 (1. 캐시 효과 검증 요구사항 반영)

    // stages: [
    //     { duration: '10s', target: 100 }, // 10초 동안 0명에서 100명까지 증가 (Ramp-up)
    //     { duration: '20s', target: 100 }, // 20초 동안 100명 유지
    // ],
    //
    // vus: 50,
    // duration: '30s',
    // summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    //
    // thresholds: {
    //     // v2의 경우 거의 모든 요청이 10ms 이내여야 함 (캐시 히트)
    //     'http_req_duration{version:v2}': ['p(95)<50'],
    //     // 에러 발생 여부 체크
    //     http_req_failed: ['rate<0.01'],
    // },

    vus: 50,          // 50명 고정
    duration: '30s',  // 30초 동안 실행
    thresholds: {
        'http_req_failed': ['rate<0.01'], // 에러율 1% 미만 유지
    },

};

export default function () {
    const version = __ENV.VERSION || 'v1';

    // 1-1 핵심: 모든 유저가 완전히 동일한 키워드와 파라미터를 사용함
    // 포트 8090 반영
    const keyword = 'apple';
    const url = `http://host.docker.internal:8090/api/products/search/${version}?keyword=${keyword}`;    // 요청 시 태그를 달아두면 나중에 v1, v2 결과를 분류해서 보기 편함
    const params = {
        tags: { version: version },
    };

    const res = http.get(url, params);

    check(res, {
        'is status 200': (r) => r.status === 200,
        'has search results': (r) => {
            const body = JSON.parse(r.body);
            return body.content && body.content.length > 0;
        },
    });

    // 0.1초 대기 (사용자가 결과를 훑어보는 시간 시뮬레이션)
    sleep(0.1);
}