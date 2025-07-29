package redis.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Configuration {
    private final Property<Integer> port = new Property<>("port", Integer::parseInt, 6379);
    private final Property<String> directory = new Property<>("dir", Function.identity());
    private final Property<String> databaseFilename = new Property<>("dbfilename", Function.identity());

    private final List<Property<?>> properties = Arrays.asList(port, directory, databaseFilename);

    public Property<?> getProperty(String key) {
        for (final var property : properties) {
            if (property.key().equalsIgnoreCase(key)) {
                return property;
            }
        }
        return null;
    }

    public Property<Integer> port() {
        return port;
    }

    public Property<String> directory() {
        return directory;
    }

    public Property<String> dbFilename() {
        return databaseFilename;
    }
}
