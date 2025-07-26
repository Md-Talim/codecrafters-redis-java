package redis;

import java.util.HashMap;
import java.util.Map;

import redis.command.Command;
import redis.command.ConfigCommand;
import redis.command.EchoCommand;
import redis.command.GetCommand;
import redis.command.KeysCommand;
import redis.command.PingCommand;
import redis.command.SetCommand;
import redis.configuration.Configuration;
import redis.store.Storage;
import redis.type.RArray;
import redis.type.RValue;

public class Evaluator {
    private final Map<String, Command> commands = new HashMap<>();

    public Evaluator(Storage storage, Configuration configuration) {
        Command pingCommand = new PingCommand();
        Command echoCommand = new EchoCommand();
        Command setCommand = new SetCommand(storage);
        Command getCommand = new GetCommand(storage);
        Command configCommand = new ConfigCommand(configuration);
        Command keysCommand = new KeysCommand(storage);
        commands.put(pingCommand.getName(), pingCommand);
        commands.put(echoCommand.getName(), echoCommand);
        commands.put(setCommand.getName(), setCommand);
        commands.put(getCommand.getName(), getCommand);
        commands.put(configCommand.getName(), configCommand);
        commands.put(keysCommand.getName(), keysCommand);
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
