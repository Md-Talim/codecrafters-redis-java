package redis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandRegistry;
import redis.command.CommandResponse;
import redis.configuration.Configuration;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.store.Storage;

public class Redis {

    private final Storage storage;
    private final Configuration configuration;
    private Map<String, Command> commands;
    private final List<Client> replicas = Collections.synchronizedList(
        new ArrayList<>()
    );
    private AtomicLong replicationOffset = new AtomicLong();

    public Redis(Storage storage, Configuration configuration) {
        this.storage = storage;
        this.configuration = configuration;
        initializeCommands();
    }

    private void initializeCommands() {
        this.commands = CommandRegistry.initializeCommands(this);
    }

    public List<Client> replicas() {
        return replicas;
    }

    public void addReplica(Client client) {
        replicas.add(client);
    }

    public void propagate(RArray command) {
        for (Client replica : replicas) {
            replica.command(new CommandResponse(command));
        }
    }

    public Storage storage() {
        return storage;
    }

    public Configuration configuration() {
        return configuration;
    }

    public AtomicLong getReplicationOffset() {
        return replicationOffset;
    }

    public CommandResponse evaluate(Client client, RValue command, long read) {
        try {
            if (command instanceof RArray rArray) {
                return evaluateArray(client, rArray);
            }

            return new CommandResponse(
                new SimpleError("ERR command must be in an array")
            );
        } finally {
            var offset = replicationOffset.addAndGet(read);
            System.out.println("offset: %s".formatted(offset));
        }
    }

    private CommandResponse evaluateArray(Client client, RArray array) {
        if (array.isEmpty()) {
            return null;
        }

        Command command = commands.get(array.getCommandName());
        if (command != null) {
            return command.execute(client, array);
        }

        return null;
    }
}
