package redis.command.sortedset;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class ZRemCommand implements Command {

    private final Storage storage;

    public ZRemCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.size() != 2) {
            return new CommandResponse(SimpleErrors.wrongArguments("zrem"));
        }

        String key = args.get(0).toString();
        String member = args.get(1).toString();

        var sortedSet = storage.getSortedSet(key);
        if (sortedSet == null) {
            return new CommandResponse(new RInteger(0));
        }

        boolean isRemoved = sortedSet.remove(member);

        return new CommandResponse(new RInteger(isRemoved ? 1 : 0));
    }

    @Override
    public String name() {
        return "ZREM";
    }
}
