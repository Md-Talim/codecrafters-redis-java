package redis.command.list;

import java.util.List;
import java.util.stream.Collectors;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.store.Storage;

public class LPushCommand implements Command {

    private final Redis redis;
    private final Storage storage;
    private final String WRONG_OPERATION =
        "WRONGTYPE Operation against a key holding the wrong kind of value";

    public LPushCommand(Redis redis) {
        this.redis = redis;
        this.storage = redis.storage();
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        List<RValue> newItems = args
            .stream()
            .skip(1)
            .collect(Collectors.toList())
            .reversed();
        String listKey = args.get(0).toString();
        RValue existingEntry = storage.get(listKey);

        if (existingEntry == null) {
            RArray newList = new RArray(newItems);
            storage.set(listKey, newList);
            return new CommandResponse(new RInteger(args.size() - 1));
        }

        if (!(existingEntry instanceof RArray)) {
            return new CommandResponse(new SimpleError(WRONG_OPERATION));
        }

        RArray existingList = (RArray) existingEntry;
        existingList.addAll(0, newItems);
        storage.set(listKey, existingList);

        redis.notifyKey(listKey);

        return new CommandResponse(new RInteger(existingList.size()));
    }

    @Override
    public String name() {
        return "LPUSH";
    }
}
