package redis.command.list;

import java.util.ArrayList;
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
        if (args.size() > 2) {
            return new CommandResponse(SimpleError.wrongArguments("lpop"));
        }

        String key = args.get(0).toString();
        RValue value = storage.get(key);
        int count = args.size() == 2
            ? Integer.valueOf(args.get(1).toString())
            : 1;

        if (value == null) {
            return new CommandResponse(new BulkString(null));
        }

        if (!(value instanceof RArray list)) {
            return new CommandResponse(new SimpleError(WRONG_OPERATION));
        }

        if (count == 1) {
            RValue poppedElement = list.remove(0);
            return new CommandResponse(poppedElement);
        }

        List<RValue> poppedElements = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            poppedElements.add(list.remove(0));
        }
        return new CommandResponse(new RArray(poppedElements));
    }

    @Override
    public String name() {
        return "LPOP";
    }
}
