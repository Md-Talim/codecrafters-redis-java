package redis.command.core;

import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.configuration.Configuration;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;

public class InfoCommand implements Command {

    private final Configuration configuration;

    public InfoCommand(Redis redis) {
        this.configuration = redis.configuration();
    }

    @Override
    public RValue execute(Client client, RArray command) {
        String role = configuration.isSlave() ? "slave" : "master";
        String masterReplId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
        int masterReplOffset = 0;
        String response = "role:%s\r\nmaster_replid:%s\r\nmaster_repl_offset:%d".formatted(role, masterReplId,
                masterReplOffset);
        return new BulkString(response);
    }

    @Override
    public String name() {
        return "INFO";
    }
}
