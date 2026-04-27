import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 50,
    duration: '30s',
    summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
    thresholds: {
        'http_req_failed': ['rate<0.01'],
    },
};

const version = __ENV.VERSION || 'v2';

export default function () {
    // 키워드는 하나로 고정 (캐시 키의 키워드 부분 통일)
    const keyword = encodeURIComponent("ALLDAY PROJECT 상품");

    // 페이지 번호를 0~5 사이에서 랜덤하게 선택
    const page = Math.floor(Math.random() * 6);

    // URL에 page 파라미터 추가
    const url = `http://host.docker.internal:8090/api/products/search/${version}?keyword=${keyword}&page=${page}`;

    const res = http.get(url);

    check(res, {
        'is status 200': (r) => r.status === 200,
        'has search results': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.content && body.content.length > 0;
            } catch (e) {
                return false;
            }
        },
    });

    sleep(0.1);
}