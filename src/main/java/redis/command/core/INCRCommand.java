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

        if (value instanceof BulkString string) {
            var response = new RInteger(Integer.valueOf(string.getValue()) + 1);
            storage.set(key, response);
            return new CommandResponse(response);
        }

        return new CommandResponse(new SimpleError(""));
    }

    @Override
    public String name() {
        return "INCR";
    }
}
