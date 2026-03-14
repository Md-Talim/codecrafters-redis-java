package redis.command.core;

import java.util.List;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class GetCommand implements Command {

    private final Storage storage;

    public GetCommand(Redis redis) {
        this.storage = redis.storage();
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        if (args.size() != 1) {
            return new CommandResponse(SimpleErrors.wrongArguments("get"));
        }

        String key = args.get(0).toString();
        RValue value = storage.get(key);

        if (value == null) {
            return new CommandResponse(new BulkString(null));
        }

        if (!(value instanceof BulkString bulk)) {
            return new CommandResponse(SimpleErrors.WRONG_TYPE_OPERATION);
        }

        return new CommandResponse(new BulkString(bulk.getValue()));
    }

    @Override
    public String name() {
        return "GET";
    }
}
