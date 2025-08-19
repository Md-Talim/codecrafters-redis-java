package redis.client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import redis.Redis;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.command.transaction.QueuedCommand;
import redis.resp.Deserializer;
import redis.resp.type.BulkString;
import redis.resp.type.EmptyRDBFile;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.util.TrackedInputStream;
import redis.util.TrackedOutputStream;

public class Client implements Runnable {

    private static final AtomicInteger ID_INTEGER = new AtomicInteger();
    private final int id;
    private final Socket socket;
    private final Redis evaluator;

    private final TrackedInputStream inputStream;
    private final TrackedOutputStream outputStream;

    private boolean connected;
    private Consumer<Client> disconnectedListener;
    private boolean replicate;
    private long offset = 0;
    private Consumer<Object> replicateConsumer;
    private final BlockingQueue<CommandResponse> pendingCommands =
        new ArrayBlockingQueue<>(128, true);
    private List<QueuedCommand> queuedCommands;

    private boolean inPubSubMode = false;
    private int subscriptionCount = 0;

    public Client(Socket socket, Redis evaluator) throws IOException {
        this.id = ID_INTEGER.incrementAndGet();
        this.socket = socket;
        this.evaluator = evaluator;
        this.inputStream = new TrackedInputStream(socket.getInputStream());
        this.outputStream = new TrackedOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        connected = true;
        System.out.println("%d: connected".formatted(id));

        try (socket) {
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

            if (replicate) {
                Thread.ofVirtual().start(() ->
                    handleReplicaResponses(deserializer)
                );
            }

            while (replicate && socket.isConnected()) {
                final var command = pendingCommands.poll(1, TimeUnit.MINUTES);
                if (command == null) {
                    continue;
                }

                System.out.println(
                    "%d: send command: %s".formatted(id, command)
                );

                outputStream.write(command.value().serialize());
                outputStream.flush();

                if (
                    command.value() instanceof BulkString ||
                    command.value() instanceof EmptyRDBFile
                ) {
                    offset = 0;
                    System.out.println("%d: reset offset".formatted(id));
                } else {
                    offset += outputStream.count();
                    System.out.println("%d: offset: %d".formatted(id, offset));
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

    public void command(CommandResponse pendingCommand) {
        boolean inserted = pendingCommands.offer(pendingCommand);

        if (!inserted) {
            System.out.println(
                "%d: retry queue command: %s".formatted(id, pendingCommand)
            );
            pendingCommands.add(pendingCommand);
        }
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

    public void setReplicateConsumer(Consumer<Object> replicateConsumer) {
        this.replicateConsumer = replicateConsumer;
    }

    public long getOffset() {
        return offset;
    }

    private void handleReplicaResponses(Deserializer deserializer) {
        try {
            while (socket.isConnected()) {
                RValue request = deserializer.read();
                if (request == null) {
                    break;
                }

                var consumer = replicateConsumer;
                if (consumer != null) {
                    consumer.accept(request);
                }
            }
        } catch (IOException e) {
            System.out.println("IOException " + e.getMessage());
        }
    }

    public List<QueuedCommand> getQueuedCommands() {
        return queuedCommands;
    }

    public void queueCommand(Command command, RArray raw) {
        if (!isInTransaction()) {
            return;
        }
        queuedCommands.add(new QueuedCommand(command, raw));
    }

    public boolean isInTransaction() {
        return queuedCommands != null;
    }

    public void beginTransaction() {
        queuedCommands = new ArrayList<>();
    }

    public void endTransaction() {
        queuedCommands = null;
    }

    public boolean isInSubscribedMode() {
        return inPubSubMode;
    }

    public void enterSubscribedMode() {
        inPubSubMode = true;
    }

    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    public void incrementSubscriptionCount() {
        subscriptionCount++;
    }

    public void notifySubscription(RArray message) {
        try {
            outputStream.write(message.serialize());
        } catch (IOException e) {
            System.out.println("IOException " + e.getMessage());
        }
    }
}
