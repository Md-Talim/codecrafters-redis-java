package redis.command.core;

import java.util.List;

import redis.client.Client;
import redis.command.Command;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;

public class EchoCommand implements Command {

    @Override
    public RValue execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        if (args.size() != 1) {
            return new SimpleError("ERR wrong number of arguments for 'echo' command");
        }
        return new BulkString(args.get(0).toString());
    }

    @Override
    public String name() {
        return "ECHO";
    }
}
