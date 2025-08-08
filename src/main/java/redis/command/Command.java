package redis.command;

import redis.client.Client;
import redis.resp.type.RArray;
import redis.resp.type.RValue;

public interface Command {
    RValue execute(Client client, RArray command);

    String name();
}
