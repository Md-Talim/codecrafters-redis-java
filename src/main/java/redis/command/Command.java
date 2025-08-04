package redis.command;

import java.util.List;

import redis.resp.type.RValue;

public interface Command {

    RValue execute(List<RValue> args);

    String name();
}
