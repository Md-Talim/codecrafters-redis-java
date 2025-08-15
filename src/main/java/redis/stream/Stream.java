package redis.stream;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import redis.resp.type.RValue;
import redis.stream.identifier.Identifier;
import redis.stream.identifier.MillisecondsIdentifier;
import redis.stream.identifier.UniqueIdentifier;
import redis.stream.identifier.WildcardIdentifier;

public class Stream implements RValue {

    private final List<StreamEntry> entries = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Condition dataCondition = lock.writeLock().newCondition();
    private UniqueIdentifier lastIdentifier;

    private final String XADD_ID_EQUAL_OR_SMALLER =
        "ERR The ID specified in XADD is equal or smaller than the target stream top item";
    private final String XADD_ID_GREATER_THAN_ZERO =
        "ERR The ID specified in XADD must be greater than 0-0";

    public UniqueIdentifier add(Identifier id, List<RValue> content) {
        lock.writeLock().lock();
        try {
            var unique = switch (id) {
                case MillisecondsIdentifier identifier -> getIdentifier(
                    identifier.milliseconds()
                );
                case UniqueIdentifier identifier -> identifier;
                case WildcardIdentifier _ -> getIdentifier(
                    System.currentTimeMillis()
                );
            };

            if (unique.milliseconds() == 0 && unique.sequenceNumber() == 0) {
                throw new RuntimeException(XADD_ID_GREATER_THAN_ZERO);
            }
            if (!isUnique(unique)) {
                throw new RuntimeException(XADD_ID_EQUAL_OR_SMALLER);
            }

            entries.add(new StreamEntry(unique, content));
            lastIdentifier = unique;
            dataCondition.signalAll();

            return unique;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<StreamEntry> read(Identifier fromExclusive) {
        lock.readLock().lock();

        try {
            List<StreamEntry> result = new ArrayList<StreamEntry>();
            boolean collecting = false;

            for (StreamEntry entry : entries) {
                var identifier = entry.identifier();

                if (collecting) {
                    result.add(entry);
                } else if (identifier.compareTo(fromExclusive) > 0) {
                    collecting = true;
                    result.add(entry);
                }
            }

            return result;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<StreamEntry> read(Identifier fromExclusive, Duration timeout) {
        if (fromExclusive == null) {
            fromExclusive = lastIdentifier;
        } else {
            List<StreamEntry> result = read(fromExclusive);
            if (!result.isEmpty()) {
                return result;
            }
        }

        if (awaitNewData(timeout)) {
            return read(fromExclusive);
        }

        return null;
    }

    private boolean awaitNewData(Duration timeout) {
        lock.writeLock().lock();
        try {
            if (Duration.ZERO.equals(timeout)) {
                dataCondition.await();
                return true;
            }
            return dataCondition.await(
                timeout.toMillis(),
                TimeUnit.MILLISECONDS
            );
        } catch (InterruptedException ignored) {} finally {
            lock.writeLock().unlock();
        }
        return false;
    }

    public List<StreamEntry> range(Identifier from, Identifier to) {
        lock.readLock().lock();

        try {
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
        } finally {
            lock.readLock().unlock();
        }
    }

    public UniqueIdentifier getIdentifier(long milliseconds) {
        lock.readLock().lock();

        try {
            if (!entries.isEmpty()) {
                var last = entries.getLast().identifier();
                if (last.milliseconds() == milliseconds) {
                    return new UniqueIdentifier(
                        milliseconds,
                        last.sequenceNumber() + 1
                    );
                }
            }

            long sequenceNumber = milliseconds == 0 ? 1l : 0l;

            return new UniqueIdentifier(milliseconds, sequenceNumber);
        } finally {
            lock.readLock().unlock();
        }
    }

    private boolean isUnique(UniqueIdentifier identifier) {
        if (entries.isEmpty()) {
            return true;
        }

        UniqueIdentifier last = entries.getLast().identifier();
        return identifier.compareTo(last) > 0;
    }

    @Override
    public byte[] serialize() {
        throw new UnsupportedOperationException(
            "Stream data type is not directly serializable"
        );
    }
}
