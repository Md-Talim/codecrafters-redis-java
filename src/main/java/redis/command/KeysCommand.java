package redis.command;

import java.util.ArrayList;
import java.util.List;

import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.store.Storage;

public class KeysCommand implements Command {
    private final Storage storage;

    public KeysCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public RValue execute(List<RValue> args) {
        if (args.size() != 1) {
            return new SimpleError("ERR wrong number of arguments for 'keys' command");
        }

        List<String> keyList = storage.keys();
        List<RValue> response = new ArrayList<>();
        for (String key : keyList) {
            response.add(new BulkString(key));
        }

        return new RArray(response);
    }

    @Override
    public String getName() {
        return "KEYS";
    }

}
