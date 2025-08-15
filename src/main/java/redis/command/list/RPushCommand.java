package redis.command.list;

import java.util.List;
import java.util.stream.Collectors;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.store.Storage;

public class RPushCommand implements Command {

    private final Storage storage;
    private final String WRONG_OPERATION =
        "WRONGTYPE Operation against a key holding the wrong kind of value";

    public RPushCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        List<RValue> newItems = args
            .stream()
            .skip(1)
            .collect(Collectors.toList());
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
        existingList.addAll(newItems);
        storage.set(listKey, existingList);
        return new CommandResponse(new RInteger(existingList.size()));
    }

    @Override
    public String name() {
        return "RPUSH";
    }
}
