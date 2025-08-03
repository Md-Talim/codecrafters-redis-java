package redis.command.replication;

import java.util.List;

import redis.command.Command;
import redis.resp.type.RValue;
import redis.resp.type.SimpleString;

public class PsyncComman implements Command {

    // Hardcoded master replication id
    private final String masterReplId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";

    @Override
    public RValue execute(List<RValue> args) {
        String response = "FULLRESYNC %s 0".formatted(masterReplId);
        return new SimpleString(response);
    }

    @Override
    public String getName() {
        return "PSYNC";
    }

}
