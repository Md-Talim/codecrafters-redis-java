package redis.command;

import java.util.List;

import redis.store.Storage;
import redis.type.BulkString;
import redis.type.RValue;
import redis.type.SimpleError;

public class GetCommand implements Command {
    private final Storage storage;

    public GetCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public RValue execute(List<RValue> args) {
        if (args.size() != 1) {
            return new SimpleError("ERR wrong number of arguments for 'set' command");
        }

        String key = args.get(0).toString();
        Object value = storage.get(key);

        return new BulkString(value == null ? null : value.toString());
    }

    @Override
    public String getName() {
        return "GET";
    }
}
