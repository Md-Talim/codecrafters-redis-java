package redis.command.replication;

import java.util.List;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.EmptyRDBFile;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleString;

public class PsyncCommand implements Command {

    // Hardcoded master replication id
    private final String masterReplId =
        "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    private final Redis redis;

    public PsyncCommand(Redis redis) {
        this.redis = redis;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        client.setReplicate(true);

        final var replicas = redis.replicas();
        replicas.add(client);

        if (!client.onDisconnect(replicas::remove)) {
            redis.replicas().remove(client);
            return new CommandResponse(
                new SimpleError("ERR could not enable replica")
            );
        }

        List<RValue> args = command.getArgs();
        if (args.size() != 2) {
            return new CommandResponse(
                new SimpleError(
                    "ERR wrong number of arguments for 'psync' command"
                )
            );
        }

        client.command(
            new CommandResponse(
                new SimpleString("FULLRESYNC %s 0".formatted(masterReplId))
            )
        );
        client.command(new CommandResponse(new EmptyRDBFile()));

        return null;
    }

    @Override
    public String name() {
        return "PSYNC";
    }
}
