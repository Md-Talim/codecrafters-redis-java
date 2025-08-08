package redis.command.core;

import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.configuration.Configuration;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;

public class InfoCommand implements Command {

    private final Configuration configuration;

    public InfoCommand(Redis redis) {
        this.configuration = redis.configuration();
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        String role = configuration.isSlave() ? "slave" : "master";
        String masterReplId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";
        int masterReplOffset = 0;
        String response =
            "role:%s\r\nmaster_replid:%s\r\nmaster_repl_offset:%d".formatted(
                role,
                masterReplId,
                masterReplOffset
            );
        return new CommandResponse(new BulkString(response));
    }

    @Override
    public String name() {
        return "INFO";
    }
}
