package redis.command;

import java.util.List;

import redis.type.RValue;
import redis.type.SimpleString;

public class PingCommand implements Command {
    @Override
    public RValue execute(List<RValue> args) {
        return new SimpleString("PONG");
    }

    @Override
    public String getName() {
        return "PING";
    }
}
