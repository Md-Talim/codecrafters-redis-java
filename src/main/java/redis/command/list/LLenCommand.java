package redis.command.list;

import java.util.List;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.RValue;
import redis.resp.type.SimpleErrors;
import redis.store.Storage;

public class LLenCommand implements Command {

    private final Storage storage;

    public LLenCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        String key = args.get(0).toString();
        RValue value = storage.get(key);

        if (value == null) {
            return new CommandResponse(new RInteger(0));
        }

        if (value instanceof RArray list) {
            return new CommandResponse(new RInteger(list.size()));
        }

        return new CommandResponse(SimpleErrors.WRONG_TYPE_OPERATION);
    }

    @Override
    public String name() {
        return "LLEN";
    }
}
