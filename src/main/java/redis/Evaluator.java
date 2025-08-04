package redis;

import java.util.Map;

import redis.command.Command;
import redis.command.CommandRegistry;
import redis.configuration.Configuration;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.store.Storage;

public class Evaluator {

    private final Storage storage;
    private final Configuration configuration;
    private final Map<String, Command> commands;

    public Evaluator(Storage storage, Configuration configuration) {
        this.storage = storage;
        this.configuration = configuration;
        this.commands = CommandRegistry.initializeCommands(storage, configuration);
    }

    public Storage storage() {
        return storage;
    }

    public Configuration configuration() {
        return configuration;
    }

    public RValue evaluate(RValue command) {
        if (command instanceof RArray rArray) {
            return evaluateArray(rArray);
        }

        return null;
    }

    private RValue evaluateArray(RArray array) {
        if (array.isEmpty()) {
            return null;
        }

        Command command = commands.get(array.getCommandName());
        if (command != null) {
            return command.execute(array.getArgs());
        }

        return null;
    }
}
