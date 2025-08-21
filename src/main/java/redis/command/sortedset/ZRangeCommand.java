package redis.command.sortedset;

import java.util.ArrayList;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class ZRangeCommand implements Command {

    private final Storage storage;

    public ZRangeCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.isEmpty() || args.size() > 3) {
            return new CommandResponse(SimpleErrors.wrongArguments("zrank"));
        }

        String key = args.get(0).toString();
        int start = Integer.valueOf(args.get(1).toString());
        int stop = Integer.valueOf(args.get(2).toString());

        var sortedSet = storage.getSortedSet(key);
        if (sortedSet == null) {
            return new CommandResponse(new RArray(new ArrayList<>()));
        }

        int normalizedStart = Math.max(0, start);
        int normalizedStop = Math.min(sortedSet.size() - 1, stop);

        if (normalizedStart > normalizedStop) {
            return new CommandResponse(new RArray(new ArrayList<>()));
        }

        var members = sortedSet.getRange(normalizedStart, normalizedStop + 1);
        return new CommandResponse(new RArray(members));
    }

    @Override
    public String name() {
        return "ZRANGE";
    }
}
