package redis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class Client implements Runnable {
    private static final AtomicInteger ID_INTEGER = new AtomicInteger();
    private final int id = ID_INTEGER.incrementAndGet();
    private final Socket socket;

    Client(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("%d: connected".formatted(id));

        try (socket) {
            var reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            var outputStream = socket.getOutputStream();

            String line;
            while ((line = reader.readLine()) != null) {
                if ("PING".equalsIgnoreCase(line)) {
                    outputStream.write("+PONG\r\n".getBytes());
                }

                outputStream.flush();
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("%d returned an error: %s".formatted(id, e.getMessage()));
            e.printStackTrace();
        }

        System.out.println("%d: disconnected".formatted(id));
    }
}
