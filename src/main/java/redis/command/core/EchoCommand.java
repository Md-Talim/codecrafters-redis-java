package redis.command.core;

import java.util.List;

import redis.command.Command;
import redis.resp.type.BulkString;
import redis.resp.type.RValue;

public class EchoCommand implements Command {

    @Override
    public RValue execute(List<RValue> args) {
        var firstArg = args.get(0);
        if (!(firstArg instanceof BulkString)) {
            return null;
        }
        var bulkString = (BulkString) firstArg;
        return new BulkString(bulkString.getValue());
    }

    @Override
    public String name() {
        return "ECHO";
    }

}
