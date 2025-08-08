package redis.command.core;

import redis.client.Client;
import redis.command.Command;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleString;

public class PingCommand implements Command {

    @Override
    public RValue execute(Client client, RArray command) {
        return new SimpleString("PONG");
    }

    @Override
    public String name() {
        return "PING";
    }
}
