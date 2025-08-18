package redis.command.pubsub;

import java.util.List;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.RValue;
import redis.resp.type.SimpleErrors;

public class PublishCommand implements Command {

    private final Redis redis;

    public PublishCommand(Redis redis) {
        this.redis = redis;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        if (args.size() != 2) {
            return new CommandResponse(SimpleErrors.wrongArguments("publish"));
        }

        String channel = args.get(0).toString();
        String message = args.get(1).toString();

        int clientsCount = redis.pubSub().publish(channel, message);

        return new CommandResponse(new RInteger(clientsCount));
    }

    @Override
    public String name() {
        return "PUBLISH";
    }

    @Override
    public boolean isPubSub() {
        return true;
    }
}
