import io.github.van1164.K6Executor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class K6ExecutorTest {

    @Test
    public void testK6Load() throws Exception {
        String[] checkList = {"is status 200", "response time < 500ms"};
        K6Executor executor = new K6Executor("test.js",checkList);
        try {
            boolean success = executor.runTest();
            assertTrue(success, "K6 load test failed");
        } catch (Exception e) {
            fail("Exception occurred during K6 load test: " + e.getMessage());
        }
    }
}