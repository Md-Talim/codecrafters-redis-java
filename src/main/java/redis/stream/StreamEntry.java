package redis.stream;

import java.util.List;

import redis.resp.type.RValue;
import redis.stream.identifier.UniqueIdentifier;

public record StreamEntry(UniqueIdentifier identifier, List<RValue> content) {
}
