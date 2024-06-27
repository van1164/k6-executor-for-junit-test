import com.sun.net.httpserver.HttpServer;
import io.github.van1164.K6Executor;
import io.github.van1164.result.K6Result;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class K6ExecutorTest {
    static HttpServer server;

    public K6ExecutorTest() throws IOException {
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
    public void isAllPassedTest() {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6Executor executor = K6Executor.builder().scriptPath("test.js").checkList(checkList).build();
        try {
            K6Result result =  executor.runTest();
            assertTrue(result.isAllPassed(), "K6 load test failed");
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }

    @Test
    public void isAllPassedListTest() {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6Executor executor = K6Executor.builder().scriptPath("test.js").checkList(checkList).build();
        try {
            K6Result result =  executor.runTest();
            assertTrue(result.isAllPassed(), "K6 load test failed");
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }

    @Test
    public void printResultTest() {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6Executor executor = K6Executor.builder().scriptPath("test.js").checkList(checkList).build();
        try {
            K6Result result = executor.runTest();
            result.printResult();
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }

    @Test
    public void getFailedCheckListTest() throws Exception {
        server.stop(0);
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6Executor executor = K6Executor.builder().scriptPath("test.js").checkList(checkList).build();
        try {
            K6Result result = executor.runTest();
            assertEquals(result.getFailedCheckList(), List.of("is status 200"));
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
        serverStart();
    }

    @Test
    public void getRequestCountTest() {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6Executor executor = K6Executor.builder().scriptPath("test.js").checkList(checkList).build();
        try {
            K6Result result = executor.runTest();
            assertTrue(result.httpRequestFound());
            assertEquals(result.getTotalRequest(), result.getSuccessRequest()+result.getFailRequest());
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }

    @Test
    public void getCounterTest() {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        List<String> counterList = List.of("success_check");
        K6Executor executor = K6Executor.builder()
                .scriptPath("counter_test.js")
                .checkList(checkList)
                .counterList(counterList)
                .build();
        try {
            K6Result result = executor.runTest();
            assertTrue(result.httpRequestFound());
            assertEquals(result.getTotalRequest(), result.getSuccessRequest()+result.getFailRequest());
            result.printResult();
            assertEquals(result.getCount("success_check"),result.getSuccessRequest());
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }

    @Test
    public void getArgsTest() {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        List<String> counterList = List.of("args_counterA","args_counterB");
        HashMap<String,String> args = new HashMap<>();
        args.put("TESTA","abc");
        args.put("TESTB","def");
        K6Executor executor = K6Executor.builder()
                .scriptPath("args_test.js")
                .checkList(checkList)
                .counterList(counterList)
                .args(args)
                .build();
        try {
            K6Result result = executor.runTest();
            assertTrue(result.httpRequestFound());
            assertEquals(result.getTotalRequest(), result.getSuccessRequest()+result.getFailRequest());
            assertNotNull(result.getCount("args_counterA"));
            assertNotNull(result.getCount("args_counterB"));
            assertEquals(result.getCount("args_counterA"),result.getCount("args_counterB"));
            result.printResult();
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }


    private static void serverStart() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new SimpleServerTest.MyHandler());
        server.setExecutor(null);
        server.start();
    }
}