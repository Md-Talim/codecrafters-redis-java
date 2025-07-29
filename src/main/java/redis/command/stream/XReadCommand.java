package redis.command.stream;

import java.time.Duration;
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

public class XReadCommand implements Command {
    private final Storage storage;

    public XReadCommand(Storage storage) {
        this.storage = storage;
    }

    @Override
    public RValue execute(List<RValue> args) {
        record Query(String key, Identifier identifier) {
        }
        List<Query> queries = new ArrayList<Query>();
        Duration timeout = null;

        int size = args.size();
        for (int i = 0; i < size; i++) {
            String element = args.get(i).toString();

            if ("block".equalsIgnoreCase(element)) {
                ++i;

                element = args.get(i).toString();
                timeout = Duration.ofMillis(Long.parseLong(element));
                continue;
            }

            if ("streams".equalsIgnoreCase(element)) {
                ++i;

                int remaining = size - i;
                int offset = remaining / 2;
                for (int j = 0; j < offset; j++) {
                    String key = args.get(i + j).toString();
                    element = args.get(i + offset + j).toString();
                    Identifier identifier = timeout != null && "$".equals(element) ? null : Identifier.parse(element);
                    queries.add(new Query(key, identifier));
                }

                break;
            }
        }

        if (timeout != null) {
            Query query = queries.getFirst();
            String key = query.key();
            Stream stream = (Stream) storage.get(key);
            List<StreamEntry> entries = stream.read(query.identifier(), timeout);

            if (entries == null) {
                return new BulkString(null);
            }

            List<RValue> entryResponse = new ArrayList<>();
            for (var entry : entries) {
                var entryData = List.of(new BulkString(entry.identifier().toString()), new RArray(entry.content()));
                entryResponse.add(new RArray(entryData));
            }

            entryResponse = List.of(new BulkString(key), new RArray(entryResponse));
            return new RArray(List.of(new RArray(entryResponse)));
        }

        List<RValue> fullResponse = new ArrayList<RValue>();
        for (Query query : queries) {
            var key = query.key();
            var stream = (Stream) storage.get(key);
            var entries = stream.read(query.identifier());

            List<RValue> queryResponse = new ArrayList<>();
            for (var entry : entries) {
                var entryData = List.of(new BulkString(entry.identifier().toString()), new RArray(entry.content()));
                queryResponse.add(new RArray(entryData));
            }

            queryResponse = List.of(new BulkString(key), new RArray(queryResponse));
            fullResponse.add(new RArray(queryResponse));
        }

        return new RArray(fullResponse);
    }

    @Override
    public String getName() {
        return "XREAD";
    }

}
