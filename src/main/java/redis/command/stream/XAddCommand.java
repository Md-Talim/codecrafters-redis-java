package redis.command.stream;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import redis.command.Command;
import redis.resp.type.BulkString;
import redis.resp.type.RValue;
import redis.store.Expiry;
import redis.store.Storage;
import redis.stream.Stream;
import redis.stream.identifier.Identifier;
import redis.stream.identifier.UniqueIdentifier;

public class XAddCommand implements Command {
    private final Storage storage;

    public XAddCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public RValue execute(List<RValue> args) {
        String key = args.get(0).toString();
        Identifier id = Identifier.parse(args.get(1).toString());

        List<RValue> keyValues = args.subList(2, args.size() - 1);

        var newIdReference = new AtomicReference<UniqueIdentifier>();
        storage.append(
                key,
                Stream.class,
                () -> Expiry.never(new Stream()),
                (stream) -> {
                    UniqueIdentifier newId = stream.add(id, keyValues);
                    newIdReference.set(newId);
                });

        return new BulkString(newIdReference.get().toString());
    }

    @Override
    public String getName() {
        return "XADD";
    }
}
