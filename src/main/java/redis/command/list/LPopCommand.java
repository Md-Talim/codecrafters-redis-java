package redis.command.list;

import java.util.List;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.store.Storage;

public class LPopCommand implements Command {

    private final Storage storage;
    private final String WRONG_OPERATION =
        "WRONGTYPE Operation against a key holding the wrong kind of value";

    public LPopCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        String key = args.get(0).toString();
        RValue value = storage.get(key);

        if (value == null) {
            return new CommandResponse(new BulkString(null));
        }

        if (value instanceof RArray list) {
            RValue poppedElement = list.remove(0);
            return new CommandResponse(poppedElement);
        }

        return new CommandResponse(new SimpleError(WRONG_OPERATION));
    }

    @Override
    public String name() {
        return "LPOP";
    }
}
