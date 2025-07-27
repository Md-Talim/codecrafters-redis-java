package redis.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Storage {
    private final Map<String, Expiry<Object>> map = new ConcurrentHashMap<>();

    public void set(String key, Object value) {
        map.put(key, Expiry.never(value));
    }

    public void set(String key, Object value, long milliseconds) {
        map.put(key, Expiry.in(value, milliseconds));
    }

    public void put(String key, Expiry<Object> value) {
        map.put(key, value);
    }

    public Object get(String key) {
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
    public <T> Expiry<T> append(String key, Class<T> type, Supplier<Expiry<T>> creator, Consumer<T> appender) {
        return (Expiry<T>) map.compute(key, (_, expiry) -> {
            if (expiry != null && (expiry.isExpired() || !expiry.isType(type))) {
                expiry = null;
            }

            if (expiry == null) {
                expiry = (Expiry<Object>) creator.get();
            }

            appender.accept((T) expiry.value());
            return expiry;
        });
    }
}
