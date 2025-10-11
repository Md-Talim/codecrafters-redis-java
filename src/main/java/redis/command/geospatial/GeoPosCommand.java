package redis.command.geospatial;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.NullArray;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class GeoPosCommand implements Command {

    private final Storage storage;

    public GeoPosCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();
        if (args.size() < 2) {
            return new CommandResponse(SimpleErrors.wrongArguments("geoadd"));
        }

        String key = args.get(0).toString();
        List<RValue> locations = args.subList(1, args.size());

        var set = storage.getSortedSet(key);
        if (set == null) {
            List<RValue> nullResponses = new ArrayList<>(
                Collections.nCopies(locations.size(), NullArray.INSTANCE)
            );
            return new CommandResponse(new RArray(nullResponses));
        }

        List<RValue> responses = new ArrayList<>();
        for (RValue location : locations) {
            var score = set.getScore(location.toString());
            if (score == null) {
                responses.add(NullArray.INSTANCE);
                continue;
            }

            // Hardcoded longitude, and latitude
            List<RValue> response = Arrays.asList(
                new BulkString("0"),
                new BulkString("0")
            );
            responses.add(new RArray(response));
        }

        return new CommandResponse(new RArray(responses));
    }

    @Override
    public String name() {
        return "GEOPOS";
    }
}
