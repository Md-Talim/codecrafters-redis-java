package redis.command.core;

import java.util.ArrayList;
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

public class KeysCommand implements Command {

    private final Storage storage;

    public KeysCommand(Redis redis) {
        this.storage = redis.storage();
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        if (args.size() != 1) {
            return new CommandResponse(
                new SimpleError(
                    "ERR wrong number of arguments for 'keys' command"
                )
            );
        }

        List<String> keyList = storage.keys();
        List<RValue> response = new ArrayList<>();
        for (String key : keyList) {
            response.add(new BulkString(key));
        }

        return new CommandResponse(new RArray(response));
    }

    @Override
    public String name() {
        return "KEYS";
    }
}
