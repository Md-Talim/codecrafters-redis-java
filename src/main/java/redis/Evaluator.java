package redis;

import java.util.HashMap;
import java.util.Map;

import redis.command.Command;
import redis.command.PingCommand;
import redis.type.RArray;
import redis.type.RValue;

public class Evaluator {
    private final Map<String, Command> commands = new HashMap<>();

    public Evaluator() {
        Command pingCommand = new PingCommand();
        commands.put(pingCommand.getName(), pingCommand);
    }

    public RValue evaluate(RValue command) {
        System.out.println("ENTERED HERE in evaluate");
        if (command instanceof RArray) {
            return evaluateArray((RArray) command);
        }

        return null;
    }

    private RValue evaluateArray(RArray array) {
        System.out.println("ENTERED HERE in evaluate Array");
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
