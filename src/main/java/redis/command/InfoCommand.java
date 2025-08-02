package redis.command;

import java.util.List;

import redis.configuration.Configuration;
import redis.resp.type.BulkString;
import redis.resp.type.RValue;

public class InfoCommand implements Command {
    private final Configuration configuration;

    public InfoCommand(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public RValue execute(List<RValue> args) {
        String action = args.get(0).toString();
        return switch (action) {
            case "replication" -> {
                String role = configuration.isSlave() ? "slave" : "master";
                yield new BulkString("role:%s".formatted(role));
            }
            default -> new BulkString("");
        };
    }

    @Override
    public String getName() {
        return "INFO";
    }

}
