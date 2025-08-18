package redis.command.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import redis.Redis;
import redis.client.Client;
import redis.command.Command;
import redis.command.CommandResponse;
import redis.configuration.Configuration;
import redis.resp.type.BulkString;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleErrors;

public class ConfigCommand implements Command {

    private final Configuration configuration;

    public ConfigCommand(Redis redis) {
        this.configuration = redis.configuration();
    }

    @Override
    public CommandResponse execute(Client client, RArray command) {
        List<RValue> args = command.getArgs();
        if (args.size() != 2) {
            return new CommandResponse(SimpleErrors.wrongArguments("config"));
        }

        String action = args.get(0).toString();
        if (!"GET".equalsIgnoreCase(action)) {
            return new CommandResponse(SimpleErrors.UNKNOWN_SUBCOMMAND);
        }

        String key = args.get(1).toString();
        var option = configuration.getOption(key);
        if (option == null) {
            return new CommandResponse(new RArray(Collections.emptyList()));
        }

        List<RValue> response = Arrays.asList(
            new BulkString(key),
            new BulkString(option.getArgumentAt(0).value().toString())
        );
        return new CommandResponse(new RArray(response));
    }

    @Override
    public String name() {
        return "CONFIG";
    }
}
