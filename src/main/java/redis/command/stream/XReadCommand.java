package redis.command.stream;

import java.util.ArrayList;
import java.util.List;

import redis.command.Command;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.store.Storage;
import redis.stream.Stream;
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

        int size = args.size();
        for (int i = 0; i < size; i++) {
            String element = args.get(i).toString();

            if ("streams".equalsIgnoreCase(element)) {
                ++i;

                int remaining = size - i;
                int offset = remaining / 2;
                for (int j = 0; j < offset; j++) {
                    String key = args.get(i + j).toString();
                    Identifier identifier = Identifier.parse(args.get(i + offset + j).toString());
                    queries.add(new Query(key, identifier));
                }

                break;
            }
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
