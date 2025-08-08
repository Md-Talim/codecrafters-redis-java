package redis.command;

import redis.resp.type.RValue;

public record CommandResponse(RValue value, boolean ignorableByReplica) {
    public CommandResponse(RValue value) {
        this(value, true);
    }
}
