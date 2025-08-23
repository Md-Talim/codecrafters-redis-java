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
import redis.command.list.BLPopCommand;
import redis.command.list.LLenCommand;
import redis.command.list.LPopCommand;
import redis.command.list.LPushCommand;
import redis.command.list.LRangeCommand;
import redis.command.list.RPushCommand;
import redis.command.pubsub.PublishCommand;
import redis.command.pubsub.SubscribeCommand;
import redis.command.pubsub.UnsubscribeCommand;
import redis.command.replication.PsyncCommand;
import redis.command.replication.ReplConfCommand;
import redis.command.replication.WaitCommand;
import redis.command.sortedset.ZAddCommand;
import redis.command.sortedset.ZCardCommand;
import redis.command.sortedset.ZRangeCommand;
import redis.command.sortedset.ZRankCommand;
import redis.command.sortedset.ZScoreCommand;
import redis.command.stream.XAddCommand;
import redis.command.stream.XRangeCommand;
import redis.command.stream.XReadCommand;
import redis.command.transaction.DiscardCommand;
import redis.command.transaction.ExecCommand;
import redis.command.transaction.MultiCommand;
import redis.store.Storage;

public class CommandRegistry {

    public static Map<String, Command> initializeCommands(Redis redis) {
        Storage storage = redis.storage();
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
            new INCRCommand(storage),
            new MultiCommand(),
            new ExecCommand(),
            new DiscardCommand(),
            new RPushCommand(redis),
            new LRangeCommand(storage),
            new LPushCommand(redis),
            new LLenCommand(storage),
            new LPopCommand(storage),
            new BLPopCommand(redis),
            new SubscribeCommand(redis),
            new PublishCommand(redis),
            new UnsubscribeCommand(redis),
            new ZAddCommand(storage),
            new ZRankCommand(storage),
            new ZRangeCommand(storage),
            new ZCardCommand(storage),
            new ZScoreCommand(storage)
        );

        return commandList
            .stream()
            .collect(Collectors.toMap(Command::name, Function.identity()));
    }
}
