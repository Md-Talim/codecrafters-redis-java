package redis.command.sortedset;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class ZRankCommand implements Command {

    private final Storage storage;

    public ZRankCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.size() == 0) {
            return new CommandResponse(SimpleErrors.wrongArguments("zrank"));
        }

        String key = args.get(0).toString();
        String member = args.get(1).toString();

        int rank = storage.getRank(key, member);

        if (rank == -1) {
            return new CommandResponse(new BulkString(null));
        }

        return new CommandResponse(new RInteger(rank));
    }

    @Override
    public String name() {
        return "ZRANK";
    }
}
