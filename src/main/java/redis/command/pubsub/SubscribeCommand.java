package redis.command.pubsub;

import java.util.List;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.RValue;
import redis.resp.type.SimpleErrors;

public class SubscribeCommand implements Command {

    private final Redis redis;

    public SubscribeCommand(Redis redis) {
        this.redis = redis;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();

        for (RValue arg : args) {
            String channel = arg.toString();

            redis.pubSub().subscribe(client, channel);

            var response = new RArray(
                List.of(
                    new BulkString("subscribe"),
                    new BulkString(channel),
                    new RInteger(client.getSubscriptionCount())
                )
            );

            return new CommandResponse(response);
        }

        // args array is empty
        return new CommandResponse(SimpleErrors.wrongArguments("subscribe"));
    }

    @Override
    public String name() {
        return "SUBSCRIBE";
    }
}
