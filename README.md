# K6-Executor-For-Junit-Test

A Executor for running K6 from Java.

## Install
gradle
```groovy
implementation 'io.github.van1164:k6-executor:0.3.2'
```
gradle.kts
```kotlin
implementation("io.github.van1164:k6-executor:0.3.2")
```

## Usage
**java**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class WebfluxSecurityExampleApplicationTests {

	@Test
	void contextLoads() throws Exception {
		String[] checkList = {"is status 200", "response time < 500ms"};
		K6Executor executor = new K6Executor("test.js",checkList);
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
		val executor = K6Executor("test.js", checkList)
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
  check(res, { 
    'is status 200': (r) => r.status === 200, 
    'response time < 500ms': (r) => r.timings.duration < 50000, 
  });

  sleep(1); 
}
```
