package redis.command.replication;

import java.util.List;

import redis.command.Command;
import redis.resp.type.RValue;
import redis.resp.type.SimpleString;

public class ReplConfCommand implements Command {

    @Override
    public RValue execute(List<RValue> args) {
        return new SimpleString("OK");
    }

    @Override
    public String getName() {
        return "REPLCONF";
    }
}
