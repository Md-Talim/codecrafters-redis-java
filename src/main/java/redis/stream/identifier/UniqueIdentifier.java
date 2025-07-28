package redis.stream.identifier;

public record UniqueIdentifier(long milliseconds, long sequenceNumber)
        implements Identifier, Comparable<UniqueIdentifier> {
    @Override
    public String toString() {
        return "%d-%d".formatted(milliseconds, sequenceNumber);
    }

    @Override
    public int compareTo(UniqueIdentifier other) {
        int compare = Long.compare(this.milliseconds, other.milliseconds);
        if (compare != 0) {
            return compare;
        }

        return Long.compare(this.sequenceNumber, other.sequenceNumber);
    }
}
