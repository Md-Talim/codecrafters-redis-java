package redis.configuration;

import java.util.Arrays;
import java.util.List;

public class Configuration {
    private final Option port = new Option("port", List.of(new PortArgument(6379)));
    private final PathOption directory = new PathOption("dir");
    private final PathOption databaseFilename = new PathOption("dbfilename");
    private final RemoteOption replicaOf = new RemoteOption("replicaof");

    private final List<Option> options = Arrays.asList(port, directory, databaseFilename, replicaOf);

    public Option getOption(String key) {
        for (final var property : options) {
            if (property.name().equalsIgnoreCase(key)) {
                return property;
            }
        }
        return null;
    }

    public Option port() {
        return port;
    }

    public PathOption directory() {
        return directory;
    }

    public PathOption databaseFilename() {
        return databaseFilename;
    }

    public RemoteOption replicaOf() {
        return replicaOf;
    }

    public List<Option> options() {
        return options;
    }

    public boolean isSlave() {
        return replicaOf.hostArgument().isSet();
    }
}