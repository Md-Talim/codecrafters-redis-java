package redis.command.transaction;

import java.util.ArrayList;
import java.util.List;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;

public class ExecCommand implements Command {

    private final String EXEC_WITHOUT_MULTI = "ERR EXEC without MULTI";

    @Override
    public CommandResponse execute(Client client, RArray command) {
        if (!client.isInTransaction()) {
            return new CommandResponse(new SimpleError(EXEC_WITHOUT_MULTI));
        }

        List<RValue> responses = new ArrayList<>();

        client.endTransaction();

        return new CommandResponse(new RArray(responses));
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public String name() {
        return "EXEC";
    }
}
