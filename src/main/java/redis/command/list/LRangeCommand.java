package redis.command.list;

import java.util.ArrayList;
import java.util.List;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.store.Storage;

public class LRangeCommand implements Command {

    private final Storage storage;

    public LRangeCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        String key = args.get(0).toString();
        int start = Integer.valueOf(args.get(1).toString());
        int stop = Integer.valueOf(args.get(2).toString());
        RValue value = storage.get(key);

        if (value == null || !(value instanceof RArray)) {
            return new CommandResponse(new RArray(new ArrayList<>()));
        }

        RArray list = (RArray) value;
        int size = list.size();

        if (start < 0) {
            start = (size + start) < 0 ? 0 : (size + start);
        }
        if (stop < 0) {
            stop = size + stop;
        }

        if (start >= size || start > stop) {
            return new CommandResponse(new RArray(new ArrayList<>()));
        } else if (stop >= size) {
            stop = size - 1;
        }

        return new CommandResponse(new RArray(list.range(start, stop)));
    }

    @Override
    public String name() {
        return "LRANGE";
    }
}
