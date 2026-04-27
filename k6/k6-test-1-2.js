import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 50,
    duration: '30s',
    // p99 지표 포함
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    thresholds: {
        'http_req_failed': ['rate<0.01'], // 에러율 1% 미만 허용
    },
};

const version = __ENV.VERSION || 'v2';

export default function () {
    // 1부터 10000 사이의 난수 생성
    const randomId = Math.floor(Math.random() * 10000) + 1;
    // 매 요청마다 apple_1, apple_8432 같은 고유한 키워드 생성
    const keyword = `apple_${randomId}`;

    const url = `http://host.docker.internal:8090/api/products/search/${version}?keyword=${keyword}`;

    const res = http.get(url);

    check(res, {
        'is status 200': (r) => r.status === 200,
        'has search results': (r) => {
            try {
                const body = JSON.parse(r.body);
                // body가 있고, 그 안에 content 배열이 있으며, 길이가 0보다 큰지 확인
                return body && body.content && body.content.length > 0;
            } catch (e) {
                // JSON 파싱 실패 시(에러 응답 등) false 반환해서 테스트 중단 방지
                return false;
            }
        },
    });

    sleep(0.1); // 1-1과 동일한 부하 간격
}