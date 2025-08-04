package redis.command.core;

import java.util.List;

import redis.command.Command;
import redis.resp.type.RValue;
import redis.resp.type.SimpleString;

public class PingCommand implements Command {

    @Override
    public RValue execute(List<RValue> args) {
        return new SimpleString("PONG");
    }

    @Override
    public String name() {
        return "PING";
    }
}
