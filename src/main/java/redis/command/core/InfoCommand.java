package redis.command.core;

import java.util.List;

import redis.command.Command;
import redis.configuration.Configuration;
import redis.resp.type.BulkString;
import redis.resp.type.RValue;

public class InfoCommand implements Command {

    private final Configuration configuration;
    private final String masterReplId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
    private final int masterReplOffset = 0;

    public InfoCommand(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public RValue execute(List<RValue> args) {
        String action = args.get(0).toString();
        return switch (action) {
            case "replication" -> {
                String role = configuration.isSlave() ? "slave" : "master";
                yield new BulkString("""
                        role:%s
                        master_replid:%s
                        master_repl_offset:%s
                         """.formatted(role, masterReplId, masterReplOffset)
                );
            }
            default ->
                new BulkString("");
        };
    }

    @Override
    public String name() {
        return "INFO";
    }

}
