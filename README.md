# K6-Executor-For-Junit-Test

A Executor for running K6 from Java.

## Install


## Usage

### Excutor with JUnit

```java
    String[] checkList = {"is status 200", "response time < 500ms"};
    K6Executor executor = new K6Executor("test.js",checkList);
    try {
        boolean success = executor.runTest();
        assertTrue(success, "K6 load test failed");
    } catch (Exception e) {
        fail("Exception occurred during K6 load test: " + e.getMessage());
    }
```

### Example k6 script

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

export let options = {
  vus: 2,
  stages: [
    { duration: '1s', target: 100 },
    { duration: '2s', target: 100 },
    { duration: '1s', target: 0 },
  ],
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
```
