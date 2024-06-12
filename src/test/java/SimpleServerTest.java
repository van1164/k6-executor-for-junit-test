import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SimpleServerTest {
    static HttpServer server;


    public SimpleServerTest() throws IOException {
    }

    @BeforeAll
    public static void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new MyHandler());
        server.setExecutor(null);
        server.start();
    }

    @AfterAll
    public static void tearDown() {
        server.stop(0);
    }


    @Test
    public void test() throws Exception {
        String url = "http://localhost:8080";
        URL obj = new URI(url).toURL();
        // HttpURLConnection 객체를 생성합니다.
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        // GET 요청을 설정합니다.
        con.setRequestMethod("GET");
        con.connect();
        // 응답 코드를 가져옵니다.
        int responseCode = con.getResponseCode();
        assertEquals(responseCode,200);
        server.stop(0);
    }


    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "Hello, World!";
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
