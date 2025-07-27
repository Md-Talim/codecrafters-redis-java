package redis.command;

import java.util.List;

import redis.resp.type.BulkString;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleString;
import redis.store.Storage;
import redis.stream.Stream;

public class TypeCommand implements Command {
    private final Storage storage;

    public TypeCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public RValue execute(List<RValue> args) {
        if (args.size() != 1) {
            return new SimpleError("ERR wrong number of arguments for 'type' command");
        }

        Object value = storage.get(args.get(0).toString());
        if (value instanceof BulkString) {
            return new SimpleString("string");
        }
        if (value instanceof Stream) {
            return new SimpleString("stream");
        }

        return new SimpleString("none");
    }

    @Override
    public String getName() {
        return "TYPE";
    }
}
