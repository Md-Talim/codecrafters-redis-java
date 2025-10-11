package redis.command.geospatial;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class GeoAddCommand implements Command {

    private final Storage storage;
    private final double MIN_LONGITUDE = -180.0;
    private final double MAX_LONGITUDE = 180.0;
    private final double MIN_LATITUDE = -85.05112878;
    private final double MAX_LATITUDE = 85.05112878;

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

        if (!isValidCoordinates(longitude, latitude)) {
            return new CommandResponse(invalidCoordinates(longitude, latitude));
        }

        return new CommandResponse(new RInteger(1));
    }

    @Override
    public String name() {
        return "GEOADD";
    }

    private boolean isValidCoordinates(double longitude, double latitude) {
        return (
            longitude >= MIN_LONGITUDE &&
            longitude <= MAX_LONGITUDE &&
            latitude >= MIN_LATITUDE &&
            latitude <= MAX_LATITUDE
        );
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
