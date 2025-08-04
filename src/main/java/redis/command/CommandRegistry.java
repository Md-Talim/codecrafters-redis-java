package redis.command;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import redis.command.core.ConfigCommand;
import redis.command.core.EchoCommand;
import redis.command.core.GetCommand;
import redis.command.core.InfoCommand;
import redis.command.core.KeysCommand;
import redis.command.core.PingCommand;
import redis.command.core.SetCommand;
import redis.command.core.TypeCommand;
import redis.command.replication.PsyncCommand;
import redis.command.replication.ReplConfCommand;
import redis.command.stream.XAddCommand;
import redis.command.stream.XRangeCommand;
import redis.command.stream.XReadCommand;
import redis.configuration.Configuration;
import redis.store.Storage;

public class CommandRegistry {

    public static Map<String, Command> initializeCommands(Storage storage, Configuration configuration) {
        List<Command> commandList = Arrays.asList(
                new PingCommand(),
                new EchoCommand(),
                new SetCommand(storage),
                new GetCommand(storage),
                new ConfigCommand(configuration),
                new KeysCommand(storage),
                new TypeCommand(storage),
                new XAddCommand(storage),
                new XRangeCommand(storage),
                new XReadCommand(storage),
                new InfoCommand(configuration),
                new ReplConfCommand(),
                new PsyncCommand()
        );

        return commandList.stream().collect(Collectors.toMap(Command::name, Function.identity()));
    }
}
