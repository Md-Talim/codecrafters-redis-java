package redis.stream.identifier;

public record UniqueIdentifier(long milliseconds, long sequenceNumber)
        implements Identifier, Comparable<Identifier> {

    @Override
    public String toString() {
        return "%d-%d".formatted(milliseconds, sequenceNumber);
    }

    @Override
    public int compareTo(Identifier other) {
        return switch (other) {
            case MillisecondsIdentifier right -> Long.compare(this.milliseconds(), right.milliseconds());
            case WildcardIdentifier _ -> 0;
            case UniqueIdentifier right -> {
                var compare = Long.compare(this.milliseconds, right.milliseconds);
                if (compare != 0) {
                    yield compare;
                }

                yield Long.compare(this.sequenceNumber(), right.sequenceNumber());
            }
        };
    }
}
