package redis.store;

public record CacheEntry<T>(T value, long until) {
    public boolean isExpired() {
        if (until == -1) {
            return false;
        }

        return System.currentTimeMillis() > until;
    }

    public static <T> CacheEntry<T> permanent(T value) {
        return new CacheEntry<T>(value, -1);
    }

    public boolean hasType(Class<?> type) {
        if (value == null) {
            return false;
        }

        return value.getClass().equals(type);
    }

    public static <T> CacheEntry<T> expiringIn(T value, long milliseconds) {
        long until = System.currentTimeMillis() + milliseconds;
        return new CacheEntry<T>(value, until);
    }
}
