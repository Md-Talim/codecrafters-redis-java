package redis.command.geospatial;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.GeoCoordinate;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class GeoAddCommand implements Command {

    private final Storage storage;

    public GeoAddCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.size() != 4) {
            return new CommandResponse(SimpleErrors.wrongArguments("geoadd"));
        }

        String key = args.get(0).toString();
        double longitude = Double.parseDouble(args.get(1).toString());
        double latitude = Double.parseDouble(args.get(2).toString());
        String member = args.get(3).toString();

        var coordinate = new GeoCoordinate(longitude, latitude);
        if (!coordinate.isValid()) {
            return new CommandResponse(invalidCoordinates(longitude, latitude));
        }

        long score = coordinate.encode();

        storage.addToSet(key, member, (long) score);

        return new CommandResponse(new RInteger(1));
    }

    @Override
    public String name() {
        return "GEOADD";
    }

    private SimpleError invalidCoordinates(double longitude, double latitude) {
        String errorMessage = String.format(
            "ERR invalid longitude,latitude pair %f,%f",
            longitude,
            latitude
        );
        return new SimpleError(errorMessage);
    }
}
