import com.sun.net.httpserver.HttpServer;
import io.github.van1164.K6Executor;
import io.github.van1164.result.K6Result;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
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
    public void isAllPassedTest() throws Exception {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6Executor executor = new K6Executor("test.js",checkList);
        try {
            K6Result result =  executor.runTest();
            assertTrue(result.isAllPassed(), "K6 load test failed");
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }

    @Test
    public void isAllPassedListTest() throws Exception {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6Executor executor = new K6Executor("test.js",checkList);
        try {
            K6Result result =  executor.runTest();
            assertTrue(result.isAllPassed(), "K6 load test failed");
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }

    @Test
    public void printResultTest() throws Exception {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6Executor executor = new K6Executor("test.js",checkList);
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
        K6Executor executor = new K6Executor("test.js",checkList);
        try {
            K6Result result = executor.runTest();
            assertEquals(result.getFailedCheckList(), List.of("is status 200"));
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
        serverStart();
    }

    @Test
    public void getRequestCountTest() throws Exception {
        List<String> checkList = List.of("is status 200", "response time < 500ms");
        K6Executor executor = new K6Executor("test.js",checkList);
        try {
            K6Result result = executor.runTest();
            assertTrue(result.httRequestFound());
            assertEquals(result.getTotalRequest(), result.getSuccessRequest()+result.getFailRequest());
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