package redis.command.geospatial;

import java.util.ArrayList;
import java.util.List;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.GeoCoordinate;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class GeoSearchCommand implements Command {

    private final Storage storage;

    public GeoSearchCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.size() != 7) {
            return new CommandResponse(SimpleErrors.wrongArguments("geodist"));
        }

        String key = args.get(0).toString();
        String searchMode = args.get(1).toString();
        double longitude = Double.parseDouble(args.get(2).toString());
        double latitude = Double.parseDouble(args.get(3).toString());
        String searchOption = args.get(4).toString();
        double radius = Double.parseDouble(args.get(5).toString());
        String unit = args.get(6).toString(); // m, km, mi

        if (
            !searchMode.equalsIgnoreCase("FROMLONLAT") ||
            !searchOption.equalsIgnoreCase("BYRADIUS")
        ) {
            return new CommandResponse(SimpleErrors.UNKNOWN_SUBCOMMAND);
        }

        var set = storage.getSortedSet(key);
        if (set == null) {
            return new CommandResponse(new BulkString(null));
        }

        var members = set.getMembers();
        var centerPoint = new GeoCoordinate.Coordinate(latitude, longitude);
        radius = convertToMeters(radius, unit);

        List<RValue> locationResponseList = new ArrayList<>();

        for (var member : members) {
            var score = set.getScore(member.toString());
            var locationCoordinate = GeoCoordinate.decode(score.longValue());
            var distance = GeoCoordinate.calculateDistance(
                centerPoint,
                locationCoordinate
            );

            if (distance <= radius) {
                locationResponseList.add(new BulkString(member.toString()));
            }
        }

        return new CommandResponse(new RArray(locationResponseList));
    }

    private double convertToMeters(double value, String unit) {
        switch (unit.toLowerCase()) {
            case "m":
                return value;
            case "km":
                return value * 1000;
            case "mi":
                return value * 1609.344; // 1 mile = 1609.344 meters
            case "ft":
                return value * 0.3048; // 1 foot = 0.3048 meters
            default:
                throw new IllegalArgumentException("Unsupported unit: " + unit);
        }
    }

    @Override
    public String name() {
        return "GEOSEARCH";
    }
}
