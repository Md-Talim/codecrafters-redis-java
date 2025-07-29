package redis.command;

import java.util.List;

import redis.resp.type.BulkString;
import redis.resp.type.RValue;

public class InfoCommand implements Command {

    @Override
    public RValue execute(List<RValue> args) {
        String action = args.get(0).toString();
        return switch (action) {
            case "replication" -> new BulkString("role:master");
            default -> new BulkString("");
        };
    }

    @Override
    public String getName() {
        return "INFO";
    }

}
