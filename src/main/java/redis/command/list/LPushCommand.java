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
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class LPushCommand implements Command {

    private final Redis redis;
    private final Storage storage;

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
            redis.notifyKey(listKey);
            return new CommandResponse(new RInteger(args.size() - 1));
        }

        if (!(existingEntry instanceof RArray)) {
            return new CommandResponse(SimpleErrors.WRONG_TYPE_OPERATION);
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
