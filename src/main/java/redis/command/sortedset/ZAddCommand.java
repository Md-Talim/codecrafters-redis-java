package redis.command.sortedset;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class ZAddCommand implements Command {

    private final Storage storage;

    public ZAddCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.isEmpty() || args.size() < 3) {
            return new CommandResponse(SimpleErrors.wrongArguments("zadd"));
        }

        String key = args.get(0).toString();
        double score = Double.parseDouble(args.get(1).toString());
        String member = args.get(2).toString();

        var response = storage.addToSet(key, member, score);

        return new CommandResponse(new RInteger(response ? 1 : 0));
    }

    @Override
    public String name() {
        return "ZADD";
    }
}
