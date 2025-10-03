package redis.command.geospatial;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.store.Storage;

public class GeoAddCommand implements Command {

    private final Storage storage;

    public GeoAddCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        return new CommandResponse(new RInteger(1));
    }

    @Override
    public String name() {
        return "GEOADD";
    }
}
