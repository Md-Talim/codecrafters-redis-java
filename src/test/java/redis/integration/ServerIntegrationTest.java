package redis.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import redis.Main;

class ServerIntegrationTest {

    private static int port;
    private static Thread serverThread;

    @BeforeAll
    static void startServer() throws Exception {
        try (var ss = new ServerSocket(0)) {
            port = ss.getLocalPort();
        }

        serverThread = Thread.ofVirtual().start(() -> {
            try {
                Main.main(new String[] { "--port", String.valueOf(port) });
            } catch (Exception e) {
                // server shut down
            }
        });

        Thread.sleep(500);
    }

    @AfterAll
    static void stopServer() {
        serverThread.interrupt();
    }

    private String sendCommand(String... parts) throws Exception {
        try (var socket = new Socket("localhost", port)) {
            OutputStream out = socket.getOutputStream();
            InputStream in = socket.getInputStream();

            StringBuilder resp = new StringBuilder();
            resp.append("*").append(parts.length).append("\r\n");
            for (String part : parts) {
                resp.append("$").append(part.length()).append("\r\n");
                resp.append(part).append("\r\n");
            }

            out.write(resp.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();

            Thread.sleep(100);
            byte[] buf = new byte[4096];
            int len = in.read(buf);
            return new String(buf, 0, len, StandardCharsets.UTF_8);
        }
    }

    @Test
    void pingReturnsPong() throws Exception {
        String response = sendCommand("PING");
        assertThat(response).contains("PONG");
    }

    @Test
    void echoReturnsArgument() throws Exception {
        String response = sendCommand("ECHO", "hello");
        assertThat(response).contains("hello");
    }

    @Test
    void setAndGet() throws Exception {
        sendCommand("SET", "testkey", "testvalue");
        String response = sendCommand("GET", "testkey");
        assertThat(response).contains("testvalue");
    }

    @Test
    void getMissingKeyReturnsNil() throws Exception {
        String response = sendCommand("GET", "nonexistent_key_xyz");
        assertThat(response).contains("$-1");
    }
}
