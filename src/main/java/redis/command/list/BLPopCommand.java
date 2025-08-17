package redis.command.list;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class BLPopCommand implements Command {

    private final Redis redis;
    private final Storage storage;

    public BLPopCommand(Redis redis) {
        this.redis = redis;
        this.storage = redis.storage();
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        String key = args.get(0).toString();
        RValue value = storage.get(key);

        Duration timeout = args.size() == 2
            ? Duration.ofMillis(
                (long) (Double.parseDouble(args.get(1).toString()) * 1000)
            )
            : Duration.ZERO;

        if (
            value == null || !(value instanceof RArray list) || list.isEmpty()
        ) {
            if (timeout.isPositive()) {
                value = redis.awaitKey(key, Optional.of(timeout));
            } else {
                value = redis.awaitKey(key, Optional.empty());
            }

            if (value == null) {
                return new CommandResponse(new BulkString(null));
            }

            if (!(value instanceof RArray)) {
                return new CommandResponse(SimpleErrors.WRONG_TYPE_OPERATION);
            }
        }

        RValue poppedElement = ((RArray) value).remove(0);
        List<RValue> response = List.of(new BulkString(key), poppedElement);
        return new CommandResponse(new RArray(response));
    }

    @Override
    public String name() {
        return "BLPOP";
    }
}
