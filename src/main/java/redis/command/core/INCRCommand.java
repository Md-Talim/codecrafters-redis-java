package redis.command.core;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.SimpleError;
import redis.store.Storage;

public class INCRCommand implements Command {

    private final Storage storage;

    public INCRCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        String key = args.get(0).toString();
        var value = storage.get(key);

        if (value == null) {
            storage.set(key, new BulkString("1"));
            return new CommandResponse(new RInteger(1));
        }

        if (value instanceof BulkString string) {
            int previousValue = Integer.valueOf(string.getValue());
            int newValue = previousValue + 1;
            storage.set(key, new BulkString(newValue));
            return new CommandResponse(new RInteger(newValue));
        }

        return new CommandResponse(new SimpleError(""));
    }

    @Override
    public String name() {
        return "INCR";
    }
}
