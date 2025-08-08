package redis.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import redis.Redis;
import redis.resp.Deserializer;
import redis.resp.type.RValue;
import redis.util.TrackedInputStream;

public class Client implements Runnable {

    private static final AtomicInteger ID_INTEGER = new AtomicInteger();
    private final int id;
    private final Socket socket;
    private final Redis evaluator;

    private boolean connected;
    private Consumer<Client> disconnectedListener;
    private boolean replicate;
    private final BlockingQueue<RValue> pendingCommands =
        new ArrayBlockingQueue<>(128, true);

    public Client(Socket socket, Redis evaluator) throws IOException {
        this.id = ID_INTEGER.incrementAndGet();
        this.socket = socket;
        this.evaluator = evaluator;
    }

    @Override
    public void run() {
        connected = true;
        System.out.println("%d: connected".formatted(id));

        try (socket) {
            final var inputStream = new TrackedInputStream(
                new BufferedInputStream(socket.getInputStream())
            );
            final var outputStream = new BufferedOutputStream(
                socket.getOutputStream()
            );
            final var deserializer = new Deserializer(inputStream);

            while (!replicate) {
                inputStream.begin();

                RValue request = deserializer.read();
                if (request == null) {
                    break;
                }

                long read = inputStream.count();
                var response = evaluator.evaluate(this, request, read);
                if (response == null) {
                    System.out.println("%d: no answer".formatted(id));
                    continue;
                }

                outputStream.write(response.value().serialize());
                outputStream.flush();
            }

            while (replicate && socket.isConnected()) {
                final var command = pendingCommands.poll(1, TimeUnit.MINUTES);
                if (command == null) {
                    continue;
                }

                System.out.println(
                    "%d: send command: %s".formatted(id, command)
                );

                if (socket.isConnected()) {
                    outputStream.write(command.serialize());
                    outputStream.flush();
                }
            }
        } catch (IOException e) {
            System.out.println(
                "%d: returned an error: %s".formatted(id, e.getMessage())
            );
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        System.out.println("%d: connected".formatted(id));

        synchronized (this) {
            connected = false;
            if (disconnectedListener != null) {
                disconnectedListener.accept(this);
            }
        }
    }

    public void setReplicate(boolean replicate) {
        this.replicate = replicate;
    }

    public void command(RValue value) {
        System.out.println("%d: queue command: %s".formatted(id, value));
        pendingCommands.add(value);
    }

    public boolean onDisconnect(Consumer<Client> listener) {
        synchronized (this) {
            if (!connected) {
                return false;
            }

            if (disconnectedListener != null) {
                return false;
            }

            disconnectedListener = listener;
            return true;
        }
    }
}
