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
        var distance = calculateDistance(coordinate1, coordinate2);

        return new CommandResponse(new BulkString("%f".formatted(distance)));
    }

    // https://rosettacode.org/wiki/Haversine_formula#Java
    private double calculateDistance(
        GeoCoordinate.Coordinate coordinate1,
        GeoCoordinate.Coordinate coordinate2
    ) {
        final double R = 6372797.560856; // In kilometers
        double lat1 = Math.toRadians(coordinate1.latitude());
        double lat2 = Math.toRadians(coordinate2.latitude());
        double dLat = lat2 - lat1;
        double dLon = Math.toRadians(
            coordinate2.longitude() - coordinate1.longitude()
        );

        double a =
            Math.pow(Math.sin(dLat / 2), 2) +
            Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);

        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }

    @Override
    public String name() {
        return "GEODIST";
    }
}
