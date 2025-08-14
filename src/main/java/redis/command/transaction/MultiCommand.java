package redis.command.transaction;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleString;

public class MultiCommand implements Command {

    private final String ALREADY_IN_TRANSCATION = "ERR already in transaction";

    @Override
    public CommandResponse execute(Client client, RArray command) {
        if (client.isInTransaction()) {
            return new CommandResponse(new SimpleError(ALREADY_IN_TRANSCATION));
        }

        client.beginTransaction();

        return new CommandResponse(new SimpleString("OK"));
    }

    @Override
    public String name() {
        return "MULTI";
    }

    @Override
    public boolean isQueueable() {
        return false;
    }
}
