package redis.command.geospatial;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.GeoCoordinate;
import redis.resp.type.RArray;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class GeoDistCommand implements Command {

    private final Storage storage;

    public GeoDistCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.size() != 3) {
            return new CommandResponse(SimpleErrors.wrongArguments("geodist"));
        }

        String key = args.get(0).toString();
        String member1 = args.get(1).toString();
        String member2 = args.get(2).toString();

        var set = storage.getSortedSet(key);
        if (set == null) {
            return new CommandResponse(new BulkString(null));
        }

        var score1 = set.getScore(member1);
        var score2 = set.getScore(member2);
        if (score1 == null || score2 == null) {
            return new CommandResponse(new BulkString(null));
        }

        var coordinate1 = GeoCoordinate.decode(score1.longValue());
        var coordinate2 = GeoCoordinate.decode(score2.longValue());
        var distance = GeoCoordinate.calculateDistance(
            coordinate1,
            coordinate2
        );

        return new CommandResponse(new BulkString("%f".formatted(distance)));
    }

    @Override
    public String name() {
        return "GEODIST";
    }
}
