import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  vus: 10,
};

export default function () {
  let url = `http://localhost:8080`

  let res = http.get(url);
  check(res, { 
    'is status 200': (r) => r.status === 200, 
    'response time < 500ms': (r) => r.timings.duration < 50000, 
  });

  sleep(1); 
}