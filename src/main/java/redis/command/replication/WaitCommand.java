package redis.command.replication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.RValue;

public class WaitCommand implements Command {

    private final Redis redis;

    public WaitCommand(Redis redis) {
        this.redis = redis;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        // Parse WAIT command arguments: WAIT <num_replicas> <timeout_ms>
        var args = command.getArgs();
        int numberOfReplicas = Integer.valueOf(args.get(0).toString());
        int timeout = Integer.valueOf(args.get(1).toString());

        List<Client> replicas = redis.replicas();

        // Fast path: if no commands have been sent, return replica count immediately
        if (redis.getReplicationOffset().get() == 0) {
            return new CommandResponse(new RInteger(replicas.size()));
        }

        final var acks = new AtomicInteger();

        // Track futures for replicas that need acknowledgment checking
        final var futures = new ArrayList<Map.Entry<Client, Future<Integer>>>(
            replicas.size()
        );
        replicas.forEach(replica -> {
            // If replica has no pending commands, count it as already acknowledged
            if (replica.getOffset() == 0) {
                acks.incrementAndGet();
                return;
            }

            // Send REPLCONF GETACK to check replica's current offset
            final var future = new CompletableFuture<Integer>();
            List<RValue> response = Arrays.asList(
                new BulkString("REPLCONF"),
                new BulkString("GETACK"),
                new BulkString("*")
            );

            replica.command(new CommandResponse(new RArray(response), false));

            // Set callback to complete future when replica responds
            replica.setReplicateConsumer(_ -> future.complete(1));
            futures.add(Map.entry(replica, future));
        });

        long remaining = (long) timeout;

        // Wait for acknowledgments with timeout tracking
        for (var entry : futures) {
            // Early exit if we have enough acknowledgments
            if (acks.get() >= numberOfReplicas) {
                break;
            }

            var future = entry.getValue();
            // If time is up, check if future completed anyway
            if (remaining <= 0) {
                var isDone = future.isDone();
                if (isDone) {
                    acks.incrementAndGet();
                }

                System.out.println(
                    "no time left isDone=%s acks=%s".formatted(
                        isDone,
                        acks.get()
                    )
                );
                continue;
            }

            // Wait for this replica's response within remaining time
            long start = System.currentTimeMillis();
            try {
                future.get(remaining, TimeUnit.MILLISECONDS);
                acks.incrementAndGet();

                // Reduce remaining time for subsequent replicas
                long took = System.currentTimeMillis() - start;
                remaining -= took;
                System.out.println(
                    "future ended took=%d remaining=%d acks=%d".formatted(
                        took,
                        remaining,
                        acks.get()
                    )
                );
            } catch (TimeoutException e) {
                long took = System.currentTimeMillis() - start;
                remaining = 0; // Time exhausted
                System.out.println("future timeout took=%d".formatted(took));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Clean up replica consumers
        futures.forEach(entry -> entry.getKey().setReplicateConsumer(null));

        // Return actual number of acknowledgments received (may be != numberOfReplicas)
        return new CommandResponse(new RInteger(acks.get()));
    }

    @Override
    public String name() {
        return "WAIT";
    }
}
