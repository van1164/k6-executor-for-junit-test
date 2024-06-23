import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const successCheck = new Counter('success_check');
export let options = {
  vus: 10,
  duration :'5s'
};

export default function () {
  let url = `http://localhost:8080`

  let res = http.get(url);
  check(res, { 
    'is status 200': (r) => r.status === 200, 
    'response time < 500ms': (r) => r.timings.duration < 50000, 
  });

  if(res.status ===200){
    successCheck.add(1);
  }

  sleep(1); 
}