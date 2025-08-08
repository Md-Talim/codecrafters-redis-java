package redis.command.replication;

import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;

public class WaitCommand implements Command {

    @Override
    public CommandResponse execute(Client client, RArray command) {
        return new CommandResponse(new RInteger(0));
    }

    @Override
    public String name() {
        return "WAIT";
    }
}
