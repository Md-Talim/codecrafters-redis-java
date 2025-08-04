package redis.command.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import redis.command.Command;
import redis.configuration.Configuration;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleError;

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
        var option = configuration.getOption(key);
        if (option == null) {
            return new RArray(Collections.emptyList());
        }

        List<RValue> response = Arrays.asList(
                new BulkString(key),
                new BulkString(option.getArgumentAt(0).value().toString())
        );
        return new RArray(response);
    }

    @Override
    public String name() {
        return "CONFIG";
    }

}
