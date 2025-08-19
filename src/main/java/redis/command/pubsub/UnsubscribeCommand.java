package redis.command.pubsub;

import java.util.List;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;

public class UnsubscribeCommand implements Command {

    private final Redis redis;

    public UnsubscribeCommand(Redis redis) {
        this.redis = redis;
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        var args = command.getArgs();

        if (args.isEmpty()) {
            var subscribedChannels = redis.pubSub().getClientChannels(client);
            if (subscribedChannels.isEmpty()) {
                // Not subscribed to any channels
                RArray response = new RArray(
                    List.of(
                        new BulkString("unsubscribe"),
                        new BulkString(null),
                        new RInteger(0)
                    )
                );
                return new CommandResponse(response);
            }

            redis.pubSub().unsubscribeAll(client);

            RArray response = new RArray(
                List.of(
                    new BulkString("unsubscribe"),
                    new BulkString(null),
                    new RInteger(client.getSubscriptionCount())
                )
            );
            return new CommandResponse(response);
        }

        String channel = args.get(0).toString();

        redis.pubSub().unsubscribe(client, channel);

        RArray response = new RArray(
            List.of(
                new BulkString("unsubscribe"),
                new BulkString(channel),
                new RInteger(client.getSubscriptionCount())
            )
        );
        return new CommandResponse(response);
    }

    @Override
    public String name() {
        return "UNSUBSCRIBE";
    }

    @Override
    public boolean isPubSub() {
        return true;
    }
}
