package redis.command.stream;

import java.util.ArrayList;
import java.util.List;

import redis.command.Command;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.store.Storage;
import redis.stream.Stream;
import redis.stream.StreamEntry;
import redis.stream.identifier.Identifier;

public class XRangeCommand implements Command {
    private final Storage storage;

    public XRangeCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public RValue execute(List<RValue> args) {
        String key = args.get(0).toString();
        Identifier fromId = Identifier.parse(args.get(1).toString());
        Identifier toId = Identifier.parse(args.get(2).toString());

        Stream stream = (Stream) storage.get(key);
        List<StreamEntry> entries = stream.range(fromId, toId);

        List<RValue> response = new ArrayList<>();
        for (StreamEntry entry : entries) {
            var entryData = List.of(new BulkString(entry.identifier().toString()), new RArray(entry.content()));
            response.add(new RArray(entryData));
        }

        return new RArray(response);
    }

    @Override
    public String getName() {
        return "XRANGE";
    }

}
