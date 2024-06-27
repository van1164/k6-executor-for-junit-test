import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const argsCounterA = new Counter('args_counterA');
const argsCounterB = new Counter('args_counterB');
export let options = {
  vus: 10,
};

const testA = __ENV.TESTA;
const testB = __ENV.TESTB;

export default function () {
  let url = `http://localhost:8080`

  let res = http.get(url);
  check(res, { 
    'is status 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 50000, 
  });
  if(testA === "abc"){
    argsCounterA.add(1);
  }
  if(testB === "def"){
    argsCounterB.add(1);
  }
  sleep(1); 
}