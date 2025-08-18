package redis.command.pubsub;

import java.util.List;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RInteger;
import redis.resp.type.RValue;

public class SubscribeCommand implements Command {

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        String channel = args.get(0).toString();
        int subscriptionCount = 1;

        var response = new RArray(
            List.of(
                new BulkString("subscribe"),
                new BulkString(channel),
                new RInteger(subscriptionCount)
            )
        );

        return new CommandResponse(response);
    }

    @Override
    public String name() {
        return "SUBSCRIBE";
    }
}
