package redis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import redis.serial.Deserializer;
import redis.type.RValue;

public class Client implements Runnable {
    private static final AtomicInteger ID_INTEGER = new AtomicInteger();
    private final int id;
    private final Socket socket;
    private final Storage storage;
    private final BufferedInputStream inputStream;
    private final BufferedOutputStream outputStream;

    Client(Socket socket, Storage storage) throws IOException {
        this.socket = socket;
        this.storage = storage;
        this.id = ID_INTEGER.incrementAndGet();
        this.inputStream = new BufferedInputStream(socket.getInputStream());
        this.outputStream = new BufferedOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        System.out.println("%d: connected".formatted(id));

        final var deserializer = new Deserializer(inputStream);

        try (socket) {
            RValue command;
            while ((command = deserializer.read()) != null) {
                var evaluator = new Evaluator(storage);
                var response = evaluator.evaluate(command);

                if (response != null) {
                    outputStream.write(response.serialize());
                    outputStream.flush();
                }
            }

            socket.close();
        } catch (IOException e) {
            System.err.println("%d returned an error: %s".formatted(id, e.getMessage()));
            e.printStackTrace();
        }

        System.out.println("%d: disconnected".formatted(id));
    }
}
