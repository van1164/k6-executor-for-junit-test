import com.sun.net.httpserver.HttpServer;
import io.github.van1164.executor.K6ExecutorWithScriptPath;
import io.github.van1164.result.K6Result;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class K6ExecutorWithScriptPathTest {
    static HttpServer server;

    public K6ExecutorWithScriptPathTest() throws IOException {
    }

    @BeforeAll
    public static void setUp() throws IOException {
        serverStart();
    }

    @AfterAll
    public static void tearDown() {
        server.stop(0);
    }

    @Test
    public void isAllPassedTest() throws IOException {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder().scriptPath("test.js").checkList(checkList).build();

        K6Result result = executor.runTest();
        assertTrue(result.isAllPassed(), "K6 load test failed");

    }

    @Test
    public void isAllPassedListTest() throws IOException {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder().scriptPath("test.js").checkList(checkList).build();
        K6Result result = executor.runTest();
        assertTrue(result.isAllPassed(), "K6 load test failed");

    }

    @Test
    public void printResultTest() throws IOException {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder().scriptPath("test.js").checkList(checkList).build();

        K6Result result = executor.runTest();
        result.printResult();

    }

    @Test
    public void getFailedCheckListTest() throws Exception {
        server.stop(0);
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder().scriptPath("test.js").checkList(checkList).build();
        K6Result result = executor.runTest();
        assertEquals(result.getFailedCheckList(), List.of("is status 200"));

        serverStart();
    }

    @Test
    public void getRequestCountTest() throws IOException {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder().scriptPath("test.js").checkList(checkList).build();

        K6Result result = executor.runTest();
        assertTrue(result.httpRequestFound());
        assertEquals(result.getTotalRequest(), result.getSuccessRequest() + result.getFailRequest());

    }

    @Test
    public void getCounterTest() throws IOException {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        List<String> counterList = List.of("success_check");
        K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder()
                .scriptPath("counter_test.js")
                .checkList(checkList)
                .counterList(counterList)
                .build();

        K6Result result = executor.runTest();
        assertTrue(result.httpRequestFound());
        assertEquals(result.getTotalRequest(), result.getSuccessRequest() + result.getFailRequest());
        result.printResult();
        assertEquals(result.getCount("success_check"), result.getSuccessRequest());

    }

    @Test
    public void getArgsTest() throws IOException {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        List<String> counterList = List.of("args_counterA", "args_counterB");
        HashMap<String, String> args = new HashMap<>();
        args.put("TESTA", "abc");
        args.put("TESTB", "def");
        K6ExecutorWithScriptPath executor = K6ExecutorWithScriptPath.builder()
                .scriptPath("args_test.js")
                .checkList(checkList)
                .counterList(counterList)
                .args(args)
                .build();

        K6Result result = executor.runTest();
        assertTrue(result.httpRequestFound());
        assertEquals(result.getTotalRequest(), result.getSuccessRequest() + result.getFailRequest());
        assertNotNull(result.getCount("args_counterA"));
        assertNotNull(result.getCount("args_counterB"));
        assertEquals(result.getCount("args_counterA"), result.getCount("args_counterB"));
        result.printResult();

    }


    private static void serverStart() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new SimpleServerTest.MyHandler());
        server.setExecutor(null);
        server.start();
    }
}