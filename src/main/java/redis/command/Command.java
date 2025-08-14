package redis.command;

import redis.client.Client;
import redis.resp.type.RArray;

public interface Command {
    CommandResponse execute(Client client, RArray command);

    default boolean isQueueable() {
        return true;
    }

    String name();
}
