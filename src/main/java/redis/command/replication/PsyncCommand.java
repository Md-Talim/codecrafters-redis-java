package redis.command.replication;

import java.util.List;

import redis.command.Command;
import redis.resp.type.EmptyRDBFile;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleString;

public class PsyncCommand implements Command {

    // Hardcoded master replication id
    private final String masterReplId = "8371b4fb1155b71f4a04d3e1bc3e18c4a990aeeb";

    @Override
    public RValue execute(List<RValue> args) {
        if (args.size() != 2) {
            return new SimpleError("ERR wrong number of arguments for 'psync' command");
        }

        String replID = args.get(0).toString();
        String offset = args.get(1).toString();
        String response = "FULLRESYNC %s 0".formatted(masterReplId);
        if (replID.equals("?") && offset.equals("-1")) {
            return new EmptyRDBFile(response);
        }

        return new SimpleString(response);
    }

    @Override
    public String getName() {
        return "PSYNC";
    }

}
