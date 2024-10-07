package scriptBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.van1164.scirptBuilder.*;
import io.github.van1164.util.K6Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class K6ScriptBuilderTest {

    private K6ScriptBuilder builder;

    @BeforeEach
    public void setUp() {
        builder = new K6ScriptBuilder();
    }

    @Test
    public void testBasicScriptGeneration() throws JsonProcessingException {
        // Arrange
        HttpRequest request = new HttpRequest.Builder()
                .method(HttpMethod.GET)
                .url("https://test-api.com/get")
                .build();

        // Act
        String script = builder
                .addImport("http")
                .addHttpRequest(request)
                .build();

        // Assert
        String expectedScript = """
                import http from 'k6/http';

                export default function () {
                    let res = http.get('https://test-api.com/get');
                }
                """;
        assertEquals(expectedScript.trim(), script.trim());
    }

    @Test
    public void testPostRequestWithBody() throws JsonProcessingException {
        // Arrange
        HttpRequest request = new HttpRequest.Builder()
                .method(HttpMethod.POST)
                .url("https://test-api.com/post")
                .body(Map.of("key", "value"))
                .build();

        // Act
        String script = builder
                .addImport("http")
                .addHttpRequest(request)
                .build();

        // Assert
        String expectedScript = """
                import http from 'k6/http';

                export default function () {
                    let payload = '{"key":"value"}';
                    let res = http.post('https://test-api.com/post');
                }
                """;
        assertEquals(expectedScript.trim(), script.trim());
    }

    @Test
    public void testScriptWithVUAndDuration() throws JsonProcessingException {
        // Arrange
        HttpRequest request = new HttpRequest.Builder()
                .method(HttpMethod.GET)
                .url("https://test-api.com/get")
                .build();

        // Act
        String script = builder
                .addImport("http")
                .addVU(50)
                .addDuration("30s")
                .addHttpRequest(request)
                .build();

        // Assert
        String expectedScript = """
                import http from 'k6/http';

                export let options = {
                    'vus': 50,
                    'duration': '30s',
                };

                export default function () {
                    let res = http.get('https://test-api.com/get');
                }
                """;
        assertEquals(expectedScript.trim(), script.trim());
    }

    @Test
    public void testScriptWithCheck() throws JsonProcessingException {
        // Arrange
        HttpRequest request = new HttpRequest.Builder()
                .method(HttpMethod.GET)
                .url("https://test-api.com/get")
                .build();

        Check check = Check.statusCheck("res", 200);

        // Act
        String script = builder
                .addImport("http")
                .addImport("check")
                .addHttpRequest(request)
                .addCheck(check)
                .build();

        // Assert
        String expectedScript = """
                import http from 'k6/http';
                import check from 'k6/check';

                export default function () {
                    let res = http.get('https://test-api.com/get');
                    check(res, { 'status is 200': r => r.status === 200 });
                }
                """;
        assertEquals(expectedScript.trim(), script.trim());
    }

    @Test
    public void testScriptWithThresholds() throws JsonProcessingException {
        // Arrange
        HttpRequest request = new HttpRequest.Builder()
                .method(HttpMethod.GET)
                .url("https://test-api.com/get")
                .build();

        Threshold threshold = new Threshold(K6Constants.HTTP_REQ_DURATION, 95, 500);

        // Act
        String script = builder
                .addImport("http")
                .addHttpRequest(request)
                .addThreshold(threshold)
                .build();

        // Assert
        String expectedScript = """
                import http from 'k6/http';

                export let thresholds = {
                    'http_req_duration': ['p(95) < 500'],
                };

                export default function () {
                    let res = http.get('https://test-api.com/get');
                }
                """;
        assertEquals(expectedScript.trim(), script.trim());
    }

    @Test
    public void testFullComplexScript() throws JsonProcessingException {
        // Arrange
        HttpRequest getRequest = new HttpRequest.Builder()
                .method(HttpMethod.GET)
                .url("https://test-api.com/get")
                .build();

        HttpRequest postRequest = new HttpRequest.Builder()
                .method(HttpMethod.POST)
                .url("https://test-api.com/post")
                .body(Map.of("key", "value"))
                .build();

        Check check = Check.statusCheck("res", 200);
        Threshold threshold = new Threshold(K6Constants.HTTP_REQ_DURATION, 95, 500);

        // Act
        String script = builder
                .addImport("http")
                .addImport("check")
                .addVU(100)
                .addDuration("1m")
                .addHttpRequest(getRequest)
                .addHttpRequest(postRequest)
                .addCheck(check)
                .addThreshold(threshold)
                .build();

        // Assert
        String expectedScript = """
                import http from 'k6/http';
                import check from 'k6/check';

                export let options = {
                    'vus': 100,
                    'duration': '1m',
                };

                export let thresholds = {
                    'http_req_duration': ['p(95) < 500'],
                };

                export default function () {
                    let res = http.get('https://test-api.com/get');
                    let payload = '{"key":"value"}';
                    res = http.post('https://test-api.com/post');
                    check(res, { 'status is 200': r => r.status === 200 });
                }
                """;
        assertEquals(expectedScript.trim(), script.trim());
    }
}
