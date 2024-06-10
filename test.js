import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const statusCounter = new Counter('http_status_code');

export let options = {
  vus: 2,
  stages: [
    { duration: '1s', target: 100 },
    { duration: '2s', target: 100 },
    { duration: '1s', target: 0 },
  ],
  // thresholds: { // 성능 목표 정의
  //   http_req_duration: ['p(95)<50'],
  // },
};

export default function () {
  let url = `http://localhost:8080`

  let res = http.get(url);
  statusCounter.add(res.status);
  check(res, { 
    'is status 200': (r) => r.status === 200, 
    'response time < 500ms': (r) => r.timings.duration < 50000, 
  });

  sleep(1); 
}