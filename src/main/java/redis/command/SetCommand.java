package redis.command;

import java.util.List;

import redis.Storage;
import redis.type.RValue;
import redis.type.SimpleError;
import redis.type.SimpleString;

public class SetCommand implements Command {
    private final Storage storage;

    public SetCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public RValue execute(List<RValue> args) {
        if (args.size() != 2) {
            return new SimpleError("ERR wrong number of arguments for 'set' command");
        }

        String key = args.get(0).toString();
        Object value = args.get(1);
        storage.set(key, value);

        return new SimpleString("OK");
    }

    @Override
    public String getName() {
        return "SET";
    }
}
