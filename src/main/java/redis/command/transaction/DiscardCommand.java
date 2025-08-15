package redis.command.transaction;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.SimpleError;
import redis.resp.type.SimpleString;

public class DiscardCommand implements Command {

    private final String DISCARD_WITHOUT_MULTI = "ERR DISCARD without MULTI";

    @Override
    public CommandResponse execute(Client client, RArray command) {
        if (!client.isInTransaction()) {
            return new CommandResponse(new SimpleError(DISCARD_WITHOUT_MULTI));
        }

        client.endTransaction();

        return new CommandResponse(new SimpleString("OK"));
    }

    @Override
    public boolean isQueueable() {
        return false;
    }

    @Override
    public String name() {
        return "DISCARD";
    }
}
