package redis.command.transaction;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleString;

public class ExecCommand implements Command {

    private final String EXEC_WITHOUT_MULTI = "ERR EXEC without MULTI";

    @Override
    public CommandResponse execute(Client client, RArray command) {
        if (!client.isInTransaction()) {
            return new CommandResponse(new SimpleError(EXEC_WITHOUT_MULTI));
        }
        return new CommandResponse(new SimpleString("OK"));
    }

    @Override
    public String name() {
        return "EXEC";
    }
}
