package redis.command.core;

import java.util.List;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.SimpleString;

public class PingCommand implements Command {

    @Override
    public CommandResponse execute(Client client, RArray command) {
        if (client != null && client.isInSubscribedMode()) {
            return new CommandResponse(
                new RArray(List.of(new BulkString("pong"), new BulkString("")))
            );
        }
        return new CommandResponse(new SimpleString("PONG"));
    }

    @Override
    public String name() {
        return "PING";
    }

    @Override
    public boolean isPubSub() {
        return true;
    }
}
