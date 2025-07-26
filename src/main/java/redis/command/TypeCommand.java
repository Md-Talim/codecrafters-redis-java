package redis.command;

import java.util.List;

import redis.store.Storage;
import redis.type.BulkString;
import redis.type.RValue;
import redis.type.SimpleError;
import redis.type.SimpleString;

public class TypeCommand implements Command {
    private final Storage storage;

    public TypeCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public RValue execute(List<RValue> args) {
        if (args.size() != 1) {
            return new SimpleError("ERR wrong number of arguments for 'type' command");
        }

        Object value = storage.get(args.get(0).toString());
        if (value instanceof BulkString) {
            return new SimpleString("string");
        }

        return new SimpleString("none");
    }

    @Override
    public String getName() {
        return "TYPE";
    }
}
