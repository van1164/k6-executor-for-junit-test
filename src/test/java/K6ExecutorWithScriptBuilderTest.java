import com.sun.net.httpserver.HttpServer;
import io.github.van1164.executor.K6ExecutorWithScriptBuilder;
import io.github.van1164.executor.K6ExecutorWithScriptPath;
import io.github.van1164.result.K6Result;
import io.github.van1164.scirptBuilder.Check;
import io.github.van1164.scirptBuilder.HttpMethod;
import io.github.van1164.scirptBuilder.HttpRequest;
import io.github.van1164.scirptBuilder.K6ScriptBuilder;
import io.github.van1164.util.K6Imports;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class K6ExecutorWithScriptBuilderTest {
    static HttpServer server;

    public K6ExecutorWithScriptBuilderTest() throws IOException {
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

        HttpRequest request = new HttpRequest.Builder("res")
                .method(HttpMethod.GET)
                .url("http://localhost:8080")
                .build();

        Check statusCheck = Check.statusCheck("res", 200);
        Check responseTimeCheck = new Check("res", "response time < 500ms","(r) => r.timings.duration < 50000");
        // Act
        K6ScriptBuilder script = new K6ScriptBuilder()
                .addImport("import http from 'k6/http'")
                .addImport(K6Imports.Check)
                .addHttpRequest(request)
                .addCheck(statusCheck)
                .addCheck(responseTimeCheck);

        K6ExecutorWithScriptBuilder executor = K6ExecutorWithScriptBuilder.builder().sb(script).build();

        System.out.println(script.build());

        K6Result result = executor.runTest();
        result.printResult();
        assertTrue(result.isAllPassed(), "K6 load test failed");

    }


    private static void serverStart() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new SimpleServerTest.MyHandler());
        server.setExecutor(null);
        server.start();
    }
}