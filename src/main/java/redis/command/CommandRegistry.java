package redis.command;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import redis.Redis;
import redis.command.core.ConfigCommand;
import redis.command.core.EchoCommand;
import redis.command.core.GetCommand;
import redis.command.core.INCRCommand;
import redis.command.core.InfoCommand;
import redis.command.core.KeysCommand;
import redis.command.core.PingCommand;
import redis.command.core.SetCommand;
import redis.command.core.TypeCommand;
import redis.command.replication.PsyncCommand;
import redis.command.replication.ReplConfCommand;
import redis.command.replication.WaitCommand;
import redis.command.stream.XAddCommand;
import redis.command.stream.XRangeCommand;
import redis.command.stream.XReadCommand;

public class CommandRegistry {

    public static Map<String, Command> initializeCommands(Redis redis) {
        List<Command> commandList = Arrays.asList(
            new PingCommand(),
            new EchoCommand(),
            new SetCommand(redis),
            new GetCommand(redis),
            new ConfigCommand(redis),
            new KeysCommand(redis),
            new TypeCommand(redis),
            new XAddCommand(redis),
            new XRangeCommand(redis),
            new XReadCommand(redis),
            new InfoCommand(redis),
            new ReplConfCommand(redis),
            new PsyncCommand(redis),
            new WaitCommand(redis),
            new INCRCommand(redis.storage())
        );

        return commandList
            .stream()
            .collect(Collectors.toMap(Command::name, Function.identity()));
    }
}
