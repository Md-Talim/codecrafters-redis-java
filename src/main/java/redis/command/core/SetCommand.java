package redis.command.core;

import java.util.List;

import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleString;
import redis.store.Storage;

public class SetCommand implements Command {

    private final Redis redis;
    private final Storage storage;

    public SetCommand(Redis redis) {
        this.redis = redis;
        this.storage = redis.storage();
    }

    @Override
    public RValue execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
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

        redis.propagate(command);

        return new SimpleString("OK");
    }

    @Override
    public String name() {
        return "SET";
    }
}
