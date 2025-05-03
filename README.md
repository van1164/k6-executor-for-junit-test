# K6‑Executor‑for‑JUnit‑Test

[![Maven Central](https://img.shields.io/maven-central/v/io.github.van1164/k6-executor)](https://central.sonatype.com/namespace/io.github.van1164)
[![License](https://img.shields.io/github/license/van1164/k6-executor-for-junit-test)](LICENSE)

> **Run k6 load‑tests straight from JUnit – no local k6 required**
>
> The library downloads the right k6 binary on the fly (OS/Arch aware), executes your load test, and gives you a typed Java API to assert latency, status codes, counters, and more.

---

## ✨ Features

* **Zero local dependencies** – works on CI/CD containers too
* **Two DSLs**
    * **Script Mode** – point to an existing `*.js` k6 script
    * **Builder Mode** – build a script in plain Java using `K6ScriptBuilder`
* **Rich result object** (`K6Result`)
    * total / success / fail requests
    * custom checks & counters
    * full raw output for debugging
* **JUnit‑friendly** – designed for `assert*()` in unit/integration tests
* **Automatic binary caching** – download once, reuse later

---

## 🚀 Installation


<summary>Gradle (Groovy DSL)</summary>

```gradle
repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'io.github.van1164:k6-executor:0.7.0'
}
```

<summary>Gradle (Kotlin DSL)</summary>

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.github.van1164:k6-executor:0.7.0")
}
```

---

## 🔰 Quick Start

### 1) Run an existing k6 script file

```java
List<String> checks   = List.of("is status 200", "response time < 500ms");
List<String> counters = List.of("success_check");

Map<String,String> env = Map.of(
        "BASE_URL", "https://api.dev"
);

K6Result result = K6Executor
        .withScriptPath("perf/login.js")   // relative to project root
        .checkList(checks)
        .counterList(counters)
        .args(env)                         //   -> becomes __ENV.BASE_URL
        .build()
        .runTest();

assertTrue(result.isAllPassed());
```

### 2) Build a script in Java (no `*.js` file needed)

```java
HttpRequest req = new HttpRequest.Builder("res")
        .method(HttpMethod.GET)
        .url("http://localhost:8080/health")
        .build();

K6ScriptBuilder script = K6ScriptBuilder.builder()
        .addImport("import http from 'k6/http'")
        .addImport(K6Imports.Check)
        .addHttpRequest(req)
        .addCheck(Check.statusCheck("res", 200))
        .build();

K6Result result = K6Executor
        .withScript(script)   // runs via STDIN
        .build()
        .runTest();

System.out.println("99th‑perc duration: " + result.percentile("http_req_duration", 0.99));
```

---

## 🧪 JUnit Example – Consistency Check

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class LikeConcurrencyTests {

    @Test
    void likeLoadTest() throws Exception {
        List<String> checks = List.of("is status 200", "response time < 500ms");

        K6Result result = K6Executor
                .withScriptPath("like_test.js")
                .checkList(checks)
                .build()
                .runTest();

        assertTrue(result.isAllPassed());
        Trip trip = tripRepository.findById(tripId).orElseThrow();
        assertEquals(result.getSuccessRequest(), trip.getLikeCount());
    }
}
```

---

## 📄 Sample k6 script (`perf/login.js`)

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: 10,
  duration: '30s',
};

export default function () {
  const res = http.get(`${__ENV.BASE_URL}/login`);
  check(res, {
    'is status 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  sleep(1);
}
```

---

## 📊 Accessing Results

```java
result.printResult();           // pretty‑prints the full k6 output

int total   = result.getTotalRequest();
int success = result.getSuccessRequest();
int fail    = result.getFailRequest();

Map<String,Integer> counters = result.getCounterMap();
```
