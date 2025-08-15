package redis.command.transaction;

import redis.command.Command;
import redis.resp.type.RArray;

public record QueuedCommand(Command command, RArray args) {}
