package redis;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import redis.configuration.Configuration;
import redis.resp.Deserializer;
import redis.resp.type.RValue;
import redis.store.Storage;

public class Client implements Runnable {
    private static final AtomicInteger ID_INTEGER = new AtomicInteger();
    private final int id;
    private final Socket socket;
    private final Storage storage;
    private final Configuration configuration;

    Client(Socket socket, Storage storage, Configuration configuration) throws IOException {
        this.id = ID_INTEGER.incrementAndGet();
        this.socket = socket;
        this.storage = storage;
        this.configuration = configuration;
    }

    @Override
    public void run() {
        System.out.println("%d: connected".formatted(id));

        try (socket) {
            final var inputStream = new BufferedInputStream(socket.getInputStream());
            final var outputStream = new BufferedOutputStream(socket.getOutputStream());
            final var deserializer = new Deserializer(inputStream);

            RValue command;
            while ((command = deserializer.read()) != null) {
                var evaluator = new Evaluator(storage, configuration);
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
