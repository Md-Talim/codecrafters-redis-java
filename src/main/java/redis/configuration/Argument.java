package redis.configuration;

import java.util.function.Function;

public class Argument<T> {
    private final String key;
    private final Function<String, T> converter;
    private T value;

    public Argument(String key, Function<String, T> converter) {
        this.key = key;
        this.converter = converter;
    }

    public Argument(String key, Function<String, T> converter, T defaultValue) {
        this.key = key;
        this.converter = converter;
        this.value = defaultValue;
    }

    public String key() {
        return key;
    }

    public T value() {
        return value;
    }

    public void set(String value) {
        this.value = converter.apply(value);
    }

    public boolean isSet() {
        return value != null;
    }
}
