package redis.command.core;

import java.util.List;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
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
            return new CommandResponse(
                new SimpleError(
                    "ERR wrong number of arguments for 'get' command"
                )
            );
        }

        String key = args.get(0).toString();
        Object value = storage.get(key);

        return new CommandResponse(
            new BulkString(value == null ? null : value.toString())
        );
    }

    @Override
    public String name() {
        return "GET";
    }
}
