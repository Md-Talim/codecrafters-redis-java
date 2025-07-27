package redis.command;

import java.util.List;

import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleString;
import redis.store.Storage;

public class SetCommand implements Command {
    private final Storage storage;

    public SetCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public RValue execute(List<RValue> args) {
        if (args.size() != 2 && args.size() != 4) {
            return new SimpleError("ERR wrong number of arguments for 'set' command");
        }

        String key = args.get(0).toString();
        Object value = args.get(1);

        if (args.size() == 4) {
            String setArg = args.get(2).toString();
            if (!"PX".equalsIgnoreCase(setArg)) {
                return new SimpleError("ERR syntax error");
            }

            long milliseconds = Long.parseLong(args.get(3).toString());
            storage.set(key, value, milliseconds);
        } else {
            storage.set(key, value);
        }

        return new SimpleString("OK");
    }

    @Override
    public String getName() {
        return "SET";
    }
}
