package redis;

import java.util.HashMap;
import java.util.Map;

import redis.command.Command;
import redis.command.EchoCommand;
import redis.command.GetCommand;
import redis.command.PingCommand;
import redis.command.SetCommand;
import redis.type.RArray;
import redis.type.RValue;

public class Evaluator {
    private final Map<String, Command> commands = new HashMap<>();

    public Evaluator(Storage storage) {
        Command pingCommand = new PingCommand();
        Command echoCommand = new EchoCommand();
        Command setCommand = new SetCommand(storage);
        Command getCommand = new GetCommand(storage);
        commands.put(pingCommand.getName(), pingCommand);
        commands.put(echoCommand.getName(), echoCommand);
        commands.put(setCommand.getName(), setCommand);
        commands.put(getCommand.getName(), getCommand);
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
