package redis.command.core;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.SimpleString;

public class PingCommand implements Command {

    @Override
    public CommandResponse execute(Client client, RArray command) {
        return new CommandResponse(new SimpleString("PONG"));
    }

    @Override
    public String name() {
        return "PING";
    }
}
