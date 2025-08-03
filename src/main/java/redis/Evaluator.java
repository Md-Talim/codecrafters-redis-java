package redis;

import java.util.HashMap;
import java.util.Map;

import redis.command.Command;
import redis.command.ConfigCommand;
import redis.command.EchoCommand;
import redis.command.GetCommand;
import redis.command.InfoCommand;
import redis.command.KeysCommand;
import redis.command.PingCommand;
import redis.command.SetCommand;
import redis.command.TypeCommand;
import redis.command.replication.PsyncComman;
import redis.command.replication.ReplConfCommand;
import redis.command.stream.XAddCommand;
import redis.command.stream.XRangeCommand;
import redis.command.stream.XReadCommand;
import redis.configuration.Configuration;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.store.Storage;

public class Evaluator {

    private final Map<String, Command> commands = new HashMap<>();

    public Evaluator(Storage storage, Configuration configuration) {
        Command pingCommand = new PingCommand();
        Command echoCommand = new EchoCommand();
        Command setCommand = new SetCommand(storage);
        Command getCommand = new GetCommand(storage);
        Command configCommand = new ConfigCommand(configuration);
        Command keysCommand = new KeysCommand(storage);
        Command typeCommand = new TypeCommand(storage);
        Command xAddCommand = new XAddCommand(storage);
        Command xRangeCommand = new XRangeCommand(storage);
        Command xReadCommand = new XReadCommand(storage);
        Command infoCommand = new InfoCommand(configuration);
        Command replconfCommand = new ReplConfCommand();
        Command psyncCommand = new PsyncComman();
        commands.put(pingCommand.getName(), pingCommand);
        commands.put(echoCommand.getName(), echoCommand);
        commands.put(setCommand.getName(), setCommand);
        commands.put(getCommand.getName(), getCommand);
        commands.put(configCommand.getName(), configCommand);
        commands.put(keysCommand.getName(), keysCommand);
        commands.put(typeCommand.getName(), typeCommand);
        commands.put(xAddCommand.getName(), xAddCommand);
        commands.put(xRangeCommand.getName(), xRangeCommand);
        commands.put(xReadCommand.getName(), xReadCommand);
        commands.put(infoCommand.getName(), infoCommand);
        commands.put(replconfCommand.getName(), replconfCommand);
        commands.put(psyncCommand.getName(), psyncCommand);
    }

    public RValue evaluate(RValue command) {
        if (command instanceof RArray rArray) {
            return evaluateArray(rArray);
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
