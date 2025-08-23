package redis.command.sortedset;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class ZScoreCommand implements Command {

    private final Storage storage;

    public ZScoreCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.size() != 2) {
            return new CommandResponse(SimpleErrors.wrongArguments("zscore"));
        }

        String key = args.get(0).toString();
        String member = args.get(1).toString();

        var sortedSet = storage.getSortedSet(key);
        if (sortedSet == null) {
            return new CommandResponse(new BulkString(null));
        }

        var score = sortedSet.getScore(member);
        if (score == null) {
            return new CommandResponse(new BulkString(null));
        }

        return new CommandResponse(new BulkString(score.toString()));
    }

    @Override
    public String name() {
        return "ZSCORE";
    }
}
