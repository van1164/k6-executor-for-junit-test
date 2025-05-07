# K6‑Executor‑for‑JUnit‑Test

[![Maven Central](https://img.shields.io/maven-central/v/io.github.van1164/k6-executor)](https://central.sonatype.com/namespace/io.github.van1164)
[![License](https://img.shields.io/github/license/van1164/k6-executor-for-junit-test)](LICENSE)

> **Run k6 load‑tests straight from JUnit – no local k6 required**
>
> The library downloads the right k6 binary on the fly (OS/Arch aware), executes your load test, and gives you a typed Java API to assert latency, status codes, counters, and more.

---

## ✨ Key Features

| Category | Details |
|----------|---------|
| **No local deps** | Works on CI/CD runners without k6 pre‑install |
| **Two DSLs** | *Script Mode* – point to an existing `*.js` <br>*Builder Mode* – build inline with `K6ScriptBuilder` |
| **Rich `K6Result`** | *NEW* for k6 ≥ 1.0 – structured objects:<br>  `DurationStats`, `CounterStats`, `DataStats` (avg/min/p95, bytes/s …) |
| **TOTAL RESULTS support** | Parses `█ TOTAL RESULTS` block (total / succeeded / failed) |
| **Metric helpers** | `result.getHttpReqDuration().getAvg()` etc. – no regex needed |
---


## 🚀 Installation


<summary>Gradle (Groovy DSL)</summary>

```gradle
repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'io.github.van1164:k6-executor:0.9.0'
}
```

<summary>Gradle (Kotlin DSL)</summary>

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    testImplementation("io.github.van1164:k6-executor:0.9.0")
}
```

---

## 🔰 Quick Start

### 1) Run an existing k6 script file

```java
K6Result result = K6Executor
        .withScriptPath("perf/login.js")        // points to a file
        .checkList(List.of("is status 200"))     // assert these ✓ checks
        .args(Map.of("BASE_URL", "https://api.dev"))
        .build()
        .runTest();

assertTrue(result.isAllPassed());
System.out.println("p95 latency = " + result.getHttpReqDuration().getP95() + " ms");
```

### 2 – Build the script in Java

```java
K6ScriptBuilder sb = K6ScriptBuilder.builder()
        .addImport("import http from 'k6/http'")
        .raw("export const options = { vus: 20, duration: '30s' };")
        .raw("export default function () { http.get('https://example.com'); }")
        .build();

K6Result r = K6Executor.withScript(sb).build().runTest();
System.out.println("total reqs = " + r.getHttpReqs().getTotal());
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

## 🧪 JUnit Example – end‑to‑end SSE test (k6 xk6‑sse)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatSseLoadTest {

  @LocalServerPort int port;

  @Test void roundTrip() throws Exception {
    Map<String,String> env = Map.of(
        "BASE_URL", "http://localhost:" + port,
        "ROOM_ID",  "test-room",
        "VUS",      "50",
        "DURATION", "1m");

    K6Result res = K6Executor
        .withScriptPath("src/test/k6/sse_chat_roundtrip.js")
        .k6BinaryPath("./k6")          // ← custom xk6 build containing sse
        .args(env)
        .counterList(List.of("messages_sent", "messages_received"))
        .checkList(List.of("SSE status 200"))
        .build().runTest();

    assertEquals(0, res.getHttpReqFailed().getTotal());
    assertEquals(50, res.getCounterMap().get("messages_received"));
  }
}
```

---

## 📊 Digging into Results

```java
DurationStats d = result.getHttpReqDuration();
System.out.printf("avg=%.2f ms, p95=%.2f ms%n", d.getAvg(), d.getP95());

long bytes = result.getDataReceived().getBytes();
System.out.println("network in = " + bytes + " B");
```

Available getters _(v1.0 branch)_:

| Method | Returns |
|--------|---------|
| `getHttpReqDuration()` | `DurationStats` (ms) |
| `getHttpReqs()` | `CounterStats` |
| `getHttpReqFailed()` | `CounterStats` |
| `getIterationDuration()` | `DurationStats` |
| `getIterations()` | `CounterStats` |
| `getDataReceived()` | `DataStats` |
| `getDataSent()` | `DataStats` |
| `getChecksTotal()` / `getChecksSucceeded()` / `getChecksFailed()` | ints |

Raw metric line access:

```java
String rawLine = result.getMetrics().get("http_req_duration");
```

---
