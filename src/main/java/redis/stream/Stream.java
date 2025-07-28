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

    public UniqueIdentifier add(Identifier id, List<RValue> content) {
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

    public UniqueIdentifier getIdentifier(long milliseconds) {
        if (!entries.isEmpty()) {
            var last = entries.getLast().identifier();
            if (last.milliseconds() == milliseconds) {
                return new UniqueIdentifier(milliseconds, last.sequenceNumber() + 1);
            }
        }

        return new UniqueIdentifier(milliseconds, 0l);
    }

    public boolean isUnique(UniqueIdentifier identifier) {
        if (entries.isEmpty()) {
            return true;
        }

        UniqueIdentifier last = entries.getLast().identifier();
        return identifier.compareTo(last) > 0;
    }
}
