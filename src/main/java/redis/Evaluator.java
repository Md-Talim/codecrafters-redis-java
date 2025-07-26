package redis;

import java.util.HashMap;
import java.util.Map;

import redis.command.Command;
import redis.command.EchoCommand;
import redis.command.PingCommand;
import redis.type.RArray;
import redis.type.RValue;

public class Evaluator {
    private final Map<String, Command> commands = new HashMap<>();

    public Evaluator() {
        Command pingCommand = new PingCommand();
        Command echoCommand = new EchoCommand();
        commands.put(pingCommand.getName(), pingCommand);
        commands.put(echoCommand.getName(), echoCommand);
    }

    public RValue evaluate(RValue command) {
        if (command instanceof RArray) {
            return evaluateArray((RArray) command);
        }

        return null;
    }

    private RValue evaluateArray(RArray array) {
        if (array.isEmpty()) {
            return null;
        }

        Command command = commands.get(array.getCommandName());
        if (command != null) {
            return command.execute(array.getArgs());
        }

        return null;
    }
}
