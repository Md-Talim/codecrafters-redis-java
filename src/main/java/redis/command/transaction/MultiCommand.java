package redis.command.transaction;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.SimpleString;

public class MultiCommand implements Command {

    @Override
    public CommandResponse execute(Client client, RArray command) {
        return new CommandResponse(new SimpleString("OK"));
    }

    @Override
    public String name() {
        return "MULTI";
    }
}
