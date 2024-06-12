package util;

import io.github.van1164.result.HttpReq;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.van1164.util.K6RegexFinder.countHttpReq;

public class K6RegexFinderTest {

    @Test
    @DisplayName("Default countHttpReq")
    public void countHttpReqTest(){
        HttpReq httpReq = countHttpReq(exampleFirst);
        Assertions.assertEquals(httpReq.getSuccess(),347);
        Assertions.assertEquals(httpReq.getFail(),234);
        Assertions.assertEquals(httpReq.getTotal(),581);
    }

    @Test
    @DisplayName("HttpReq success 0 test")
    public void countHttpReqSuccessZeroTest(){
        HttpReq httpReq = countHttpReq(exampleSecond);
        Assertions.assertEquals(httpReq.getSuccess(),0);
        Assertions.assertEquals(httpReq.getFail(),234);
        Assertions.assertEquals(httpReq.getTotal(),234);
    }




    static String exampleFirst = "     checks.........................: 100.00% ✓ 694          ✗ 0    \n" +
            "     data_received..................: 31 kB   6.8 kB/s\n" +
            "     data_sent......................: 28 kB   6.1 kB/s\n" +
            "     http_req_blocked...............: avg=165.38µs min=0s med=0s      max=9.99ms p(90)=517.5µs p(95)=583.79µs\n" +
            "     http_req_connecting............: avg=111.97µs min=0s med=0s      max=3.99ms p(90)=517.5µs p(95)=583.79µs\n" +
            "     http_req_duration..............: avg=656.6µs  min=0s med=549.4µs max=6.05ms p(90)=1.09ms  p(95)=1.53ms  \n" +
            "       { expected_response:true }...: avg=656.6µs  min=0s med=549.4µs max=6.05ms p(90)=1.09ms  p(95)=1.53ms  \n" +
            "     http_req_failed................: 0.00%   ✓ 234            ✗ 347  \n" +
            "     http_req_receiving.............: avg=94.04µs  min=0s med=0s      max=4.97ms p(90)=513.6µs p(95)=521.47µs\n" +
            "     http_req_sending...............: avg=30.57µs  min=0s med=0s      max=3ms    p(90)=0s      p(95)=5.52µs  \n" +
            "     http_req_tls_handshaking.......: avg=0s       min=0s med=0s      max=0s     p(90)=0s      p(95)=0s      \n" +
            "     http_req_waiting...............: avg=531.98µs min=0s med=524.2µs max=4.28ms p(90)=1ms     p(95)=1.25ms  \n" +
            "     vus_max........................: 100     min=100        max=100";

    static String exampleSecond = "     checks.........................: 100.00% ✓ 694          ✗ 0    \n" +
            "     data_received..................: 31 kB   6.8 kB/s\n" +
            "     data_sent......................: 28 kB   6.1 kB/s\n" +
            "     http_req_blocked...............: avg=165.38µs min=0s med=0s      max=9.99ms p(90)=517.5µs p(95)=583.79µs\n" +
            "     http_req_connecting............: avg=111.97µs min=0s med=0s      max=3.99ms p(90)=517.5µs p(95)=583.79µs\n" +
            "     http_req_duration..............: avg=656.6µs  min=0s med=549.4µs max=6.05ms p(90)=1.09ms  p(95)=1.53ms  \n" +
            "       { expected_response:true }...: avg=656.6µs  min=0s med=549.4µs max=6.05ms p(90)=1.09ms  p(95)=1.53ms  \n" +
            "     http_req_failed................: 0.00%   ✓ 234            ✗ 0  \n" +
            "     http_req_receiving.............: avg=94.04µs  min=0s med=0s      max=4.97ms p(90)=513.6µs p(95)=521.47µs\n" +
            "     http_req_sending...............: avg=30.57µs  min=0s med=0s      max=3ms    p(90)=0s      p(95)=5.52µs  \n" +
            "     http_req_tls_handshaking.......: avg=0s       min=0s med=0s      max=0s     p(90)=0s      p(95)=0s      \n" +
            "     http_req_waiting...............: avg=531.98µs min=0s med=524.2µs max=4.28ms p(90)=1ms     p(95)=1.25ms  \n" +
            "     vus_max........................: 100     min=100        max=100";
}
