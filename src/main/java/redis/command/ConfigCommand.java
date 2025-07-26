package redis.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import redis.configuration.Configuration;
import redis.type.BulkString;
import redis.type.RArray;
import redis.type.RValue;
import redis.type.SimpleError;

public class ConfigCommand implements Command {
    private final Configuration configuration;

    public ConfigCommand(Configuration configutaion) {
        this.configuration = configutaion;
    }

    @Override
    public RValue execute(List<RValue> args) {
        String action = args.get(0).toString();
        if (!"GET".equalsIgnoreCase(action)) {
            return new SimpleError("ERR unknown subcommand");
        }

        String key = args.get(1).toString();
        var property = configuration.getProperty(key);
        if (property == null) {
            return new RArray(Collections.emptyList());
        }

        List<RValue> response = Arrays.asList(new BulkString(key), new BulkString(property.value()));
        return new RArray(response);
    }

    @Override
    public String getName() {
        return "CONFIG";
    }

}
