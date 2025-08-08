package redis.command.replication;

import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;

public class WaitCommand implements Command {

    private final Redis redis;

    public WaitCommand(Redis redis) {
        this.redis = redis;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        return new CommandResponse(new RInteger(redis.replicas().size()));
    }

    @Override
    public String name() {
        return "WAIT";
    }
}
