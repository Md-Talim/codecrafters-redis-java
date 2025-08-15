package redis.command.list;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
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
        var args = command.getArgs();
        String listKey = args.get(0).toString();
        var existingEntry = storage.get(listKey);

        if (existingEntry == null) {
            var newList = new ArrayList<Object>();
            var values = args.stream().skip(1).collect(Collectors.toList());
            newList.addAll(values);
            storage.set(listKey, newList);
            return new CommandResponse(new RInteger(newList.size()));
        }

        if (!(existingEntry instanceof List<?>)) {
            return new CommandResponse(new SimpleError(WRONG_OPERATION));
        }

        @SuppressWarnings("unchecked")
        List<Object> objectList = (List<Object>) existingEntry;
        var values = args.stream().skip(1).collect(Collectors.toList());
        objectList.addAll(values);
        storage.set(listKey, objectList);
        return new CommandResponse(new RInteger(objectList.size()));
    }

    @Override
    public String name() {
        return "RPUSH";
    }
}
