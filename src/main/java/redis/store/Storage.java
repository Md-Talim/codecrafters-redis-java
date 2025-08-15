package redis.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;
import redis.resp.type.RValue;

public class Storage {

    private final Map<String, CacheEntry<RValue>> map =
        new ConcurrentHashMap<>();

    public void clear() {
        map.clear();
    }

    public void set(String key, RValue value) {
        map.put(key, CacheEntry.permanent(value));
    }

    public void set(String key, RValue value, long milliseconds) {
        map.put(key, CacheEntry.expiringIn(value, milliseconds));
    }

    public void put(String key, CacheEntry<RValue> value) {
        map.put(key, value);
    }

    public RValue get(String key) {
        var expiry = map.computeIfPresent(key, (_, value) -> {
            if (value.isExpired()) {
                return null;
            }
            return value;
        });

        if (expiry != null) {
            return expiry.value();
        }

        return null;
    }

    public List<String> keys() {
        return new ArrayList<>(map.keySet());
    }

    @SuppressWarnings("unchecked")
    public <T> CacheEntry<T> append(
        String key,
        Class<T> type,
        Supplier<CacheEntry<T>> creator,
        Consumer<T> appender
    ) {
        return (CacheEntry<T>) map.compute(key, (_, expiry) -> {
            if (
                expiry != null && (expiry.isExpired() || !expiry.hasType(type))
            ) {
                expiry = null;
            }

            if (expiry == null) {
                expiry = (CacheEntry<RValue>) creator.get();
            }

            appender.accept((T) expiry.value());
            return expiry;
        });
    }
}
