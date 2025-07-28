package redis.stream;

import java.util.ArrayList;
import java.util.List;

import redis.resp.type.RValue;
import redis.stream.identifier.Identifier;
import redis.stream.identifier.MillisecondsIdentifier;
import redis.stream.identifier.UniqueIdentifier;
import redis.stream.identifier.WildcardIdentifier;

public class Stream {
    private final List<StreamEntry> entries = new ArrayList<>();
    private final String XADD_ID_EQUAL_OR_SMALLER = "ERR The ID specified in XADD is equal or smaller than the target stream top item";
    private final String XADD_ID_GREATER_THAN_ZERO = "ERR The ID specified in XADD must be greater than 0-0";

    public synchronized UniqueIdentifier add(Identifier id, List<RValue> content) {
        var unique = switch (id) {
            case MillisecondsIdentifier identifier -> getIdentifier(identifier.milliseconds());
            case UniqueIdentifier identifier -> identifier;
            case WildcardIdentifier _ -> getIdentifier(System.currentTimeMillis());
        };

        if (unique.milliseconds() == 0 && unique.sequenceNumber() == 0) {
            throw new RuntimeException(XADD_ID_GREATER_THAN_ZERO);
        }

        if (!isUnique(unique)) {
            throw new RuntimeException(XADD_ID_EQUAL_OR_SMALLER);
        }

        entries.add(new StreamEntry(unique, content));

        return unique;
    }

    public synchronized List<StreamEntry> range(Identifier from, Identifier to) {
        List<StreamEntry> result = new ArrayList<StreamEntry>();
        boolean collecting = false;

        for (StreamEntry entry : entries) {
            var identifier = entry.identifier();
            if (identifier.compareTo(to) > 0) {
                break;
            }

            if (collecting) {
                result.add(entry);
            } else if (identifier.compareTo(from) >= 0) {
                collecting = true;
                result.add(entry);
            }
        }

        return result;
    }

    public UniqueIdentifier getIdentifier(long milliseconds) {
        if (!entries.isEmpty()) {
            var last = entries.getLast().identifier();
            if (last.milliseconds() == milliseconds) {
                return new UniqueIdentifier(milliseconds, last.sequenceNumber() + 1);
            }
        }

        long sequenceNumber = milliseconds == 0 ? 1l : 0l;

        return new UniqueIdentifier(milliseconds, sequenceNumber);
    }

    public boolean isUnique(UniqueIdentifier identifier) {
        if (entries.isEmpty()) {
            return true;
        }

        UniqueIdentifier last = entries.getLast().identifier();
        return identifier.compareTo(last) > 0;
    }
}
