package redis.command.replication;

import java.util.Arrays;
import java.util.List;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleString;

public class ReplConfCommand implements Command {

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        String action = args.get(0).toString();

        if ("GETACK".equalsIgnoreCase(action)) {
            List<RValue> response = Arrays.asList(
                new BulkString("REPLCONF"),
                new BulkString("ACK"),
                new BulkString("0")
            );
            return new CommandResponse(new RArray(response), false);
        }

        return new CommandResponse(new SimpleString("OK"));
    }

    @Override
    public String name() {
        return "REPLCONF";
    }
}
