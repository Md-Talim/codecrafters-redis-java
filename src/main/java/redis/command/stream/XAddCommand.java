package redis.command.stream;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;
import redis.store.CacheEntry;
import redis.store.Storage;
import redis.stream.Stream;
import redis.stream.identifier.Identifier;
import redis.stream.identifier.UniqueIdentifier;

public class XAddCommand implements Command {

    private final Storage storage;

    public XAddCommand(Redis redis) {
        this.storage = redis.storage();
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        if (args.size() < 3) {
            return new CommandResponse(
                new SimpleError(
                    "ERR wrong number of arguments for 'xadd' command"
                )
            );
        }

        String key = args.get(0).toString();
        Identifier id = Identifier.parse(args.get(1).toString());

        List<RValue> keyValues = args.subList(2, args.size());
        var newIdReference = new AtomicReference<UniqueIdentifier>();
        try {
            storage.append(
                key,
                Stream.class,
                () -> CacheEntry.permanent(new Stream()),
                stream -> {
                    UniqueIdentifier newId = stream.add(id, keyValues);
                    newIdReference.set(newId);
                }
            );

            return new CommandResponse(
                new BulkString(newIdReference.get().toString())
            );
        } catch (Exception e) {
            return new CommandResponse(new SimpleError(e.getMessage()));
        }
    }

    @Override
    public String name() {
        return "XADD";
    }
}
