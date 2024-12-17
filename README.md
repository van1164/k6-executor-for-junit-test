# K6-Executor-For-Junit-Test

> A Executor that can run K6 only with java code even if K6 is not installed local
> 
<br>

> 로컬에 K6가 설치되어 있지 않아도 java 코드로만 K6를 실행할 수 있는 Executor

## Install
gradle
```groovy
implementation 'io.github.van1164:k6-executor:0.7.0'
```
gradle.kts
```kotlin
implementation("io.github.van1164:k6-executor:0.7.0")
```

## run test

### with js script file
```java
List<String> checkList = List.of("is status 200", "response time < 500ms");
List<String> counterList = List.of("success_check");
HashMap<String,String> args = new HashMap<>();
args.put("TESTA","abc");
args.put("TESTB","def");
K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder()
	.scriptPath("test.js")  // If you specify "test.js", it is the root path of gradle
	.checkList(checkList)	  // check list allows you to check the check specified in the script in java.
	.counterList(counterList) // counter list allows you to obtain counter added to the script.
	.args(args)		// You can put arguments to be delivered as script.
	.build();

// It also supports absolute paths.
//K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder().scriptPath("C:\\Users\\test.js",checkList);


K6Result result = executor.runTest();
```

### with k6ScriptBuilder
```java
HttpRequest request = new HttpRequest.Builder("res")
	.method(HttpMethod.GET)
	.url("http://localhost:8080")
	.build();

Check statusCheck = Check.statusCheck("res", 200);
Check responseTimeCheck = new Check("res", "response time < 500ms","(r) => r.timings.duration < 50000");

K6ScriptBuilder script = new K6ScriptBuilder()
	.addImport("import http from 'k6/http'")
	.addImport(K6Imports.Check)
	.addHttpRequest(request)
	.addCheck(statusCheck)
	.addCheck(responseTimeCheck);

K6ExecutorWithScriptBuilder executor = K6ExecutorWithScriptBuilder.builder().sb(script).build();

K6Result result = executor.runTest();

```

## printResult
code
```java
result.printResult();
```

result

```

          /\      |‾‾| /‾‾/   /‾‾/   
     /\  /  \     |  |/  /   /  /    
    /  \/    \    |     (   /   ‾‾\  
   /          \   |  |\  \ |  (‾)  | 
  / __________ \  |__| \__\ \_____/ .io

time="2024-06-13T17:54:16+09:00" level=warning msg="the `vus=10` option will be ignored, it only works in conjunction with `iterations`, `duration`, or `stages`"
     execution: local
        script: test.js
        output: -

     scenarios: (100.00%) 1 scenario, 1 max VUs, 10m30s max duration (incl. graceful stop):
              * default: 1 iterations for each of 1 VUs (maxDuration: 10m0s, gracefulStop: 30s)


running (00m01.0s), 1/1 VUs, 0 complete and 0 interrupted iterations
default   [   0% ] 1 VUs  00m01.0s/10m0s  0/1 iters, 1 per VU

     ✓ is status 200
     ✓ response time < 500ms

     checks.........................: 100.00% ✓ 2       ✗ 0  
     data_received..................: 89 B    87 B/s
     data_sent......................: 80 B    78 B/s
     http_req_blocked...............: avg=13.15ms min=13.15ms med=13.15ms max=13.15ms p(90)=13.15ms p(95)=13.15ms
     http_req_connecting............: avg=509.6µs min=509.6µs med=509.6µs max=509.6µs p(90)=509.6µs p(95)=509.6µs
     http_req_duration..............: avg=574.1µs min=574.1µs med=574.1µs max=574.1µs p(90)=574.1µs p(95)=574.1µs
       { expected_response:true }...: avg=574.1µs min=574.1µs med=574.1µs max=574.1µs p(90)=574.1µs p(95)=574.1µs
     http_req_failed................: 0.00%   ✓ 0       ✗ 1  
     http_req_receiving.............: avg=0s      min=0s      med=0s      max=0s      p(90)=0s      p(95)=0s     
     http_req_sending...............: avg=63.3µs  min=63.3µs  med=63.3µs  max=63.3µs  p(90)=63.3µs  p(95)=63.3µs 
     http_req_tls_handshaking.......: avg=0s      min=0s      med=0s      max=0s      p(90)=0s      p(95)=0s     
     http_req_waiting...............: avg=510.8µs min=510.8µs med=510.8µs max=510.8µs p(90)=510.8µs p(95)=510.8µs
     http_reqs......................: 1       0.97802/s
     iteration_duration.............: avg=1.02s   min=1.02s   med=1.02s   max=1.02s   p(90)=1.02s   p(95)=1.02s  
     iterations.....................: 1       0.97802/s
     vus............................: 1       min=1     max=1
     vus_max........................: 1       min=1     max=1


running (00m01.0s), 0/1 VUs, 1 complete and 0 interrupted iterations
default ✓ [ 100% ] 1 VUs  00m01.0s/10m0s  1/1 iters, 1 per VU

```

---

## HTTP Total Request
```java
result.getTotalRequest()	// total request : Integer
result.getSuccessRequest()	// success request : Integer
result.getFailRequest()		// fail request : Integer
```

## Example With Spring

### Example of testing concurrency issues with the Like feature

### 좋아요 기능에서 발생하는 동시성문제 테스트 예제

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class K6Tests {

    @BeforeEach
    public void before() {
    }

    @Test
    void k6ExecutorTest() throws Exception {
        System.out.print(tripId);
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder()
					.scriptPath("test.js")
					.checkList(checkList);
        try {
            K6Result result = executor.runTest();
            assertTrue(result.isAllPassed());
            Trip trip = tripRepository.findById(tripId).get();
            assertEquals(result.getSuccessRequest(),trip.getLikeCount());  // successRequest vs trip.getLikeCount
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }

}

```

Check the consistency problem by comparing the number of requests tested with K6 simultaneously with the number of likes in the DB & Check the response speed with checkList

K6로 동시에 테스트한 요청 수와 DB의 좋아요 수 비교를 통한 정합성 문제 확인 & checkList를 통한 응답속도 확인

---

## Usage
**java**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WebfluxSecurityExampleApplicationTests {

	@Test
	void contextLoads() throws Exception {
		List<String> checkList = List.of("is status 200", "response time < 500ms");
		K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder()
				.scriptPath("test.js")
				.checkList(checkList);
		try {
			K6Result result =  executor.runTest();
			assertTrue(result.isAllPassed());    //check all checkList passed
		} catch (Exception e) {
			fail("Exception occurred during K6 load test: " + e.getMessage());
		}
	}

}

```

**kotlin**

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WebfluxSecurityExampleApplicationTests {

	@Test
	fun k6ExecutorTest() {
		val checkList = listOf("is status 200", "response time < 500ms")
		val executor = K6ExecutorWithScriptPath.builder()
				.scriptPath("test.js")
				.checkList(checkList);
		try {
			val result = executor.runTest()
			assertTrue(result.isAllPassed)    //check all checkList passed
		} catch (e: Exception) {
			Assertions.fail<Any>("Exception occurred during K6 load test: " + e.message)
		}
	}

}

```

### Example k6 script

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

const testA = __ENV.TESTA;
const testB = __ENV.TESTB;

export let options = {
  vus: 2,
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
```
