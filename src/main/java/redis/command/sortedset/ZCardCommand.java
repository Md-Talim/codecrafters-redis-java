package redis.command.sortedset;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class ZCardCommand implements Command {

    private final Storage storage;

    public ZCardCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.size() != 1) {
            return new CommandResponse(SimpleErrors.wrongArguments("zcard"));
        }

        String key = args.get(0).toString();
        var sortedSet = storage.getSortedSet(key);
        if (sortedSet == null) {
            return new CommandResponse(new RInteger(0));
        }

        return new CommandResponse(new RInteger(sortedSet.size()));
    }

    @Override
    public String name() {
        return "ZCARD";
    }
}
