package redis.resp.type;

public class SimpleErrors {

    public static final SimpleError WRONG_TYPE_OPERATION = new SimpleError(
        "WRONGTYPE Operation against a key holding the wrong kind of value"
    );

    public static final SimpleError UNKNOWN_SUBCOMMAND = new SimpleError(
        "ERR unknown subcommand"
    );

    public static SimpleError wrongArguments(String command) {
        return new SimpleError(
            "ERR wrong number of arguments for '%s' command".formatted(command)
        );
    }

    public static SimpleError unknownCommand(String command) {
        return new SimpleError("ERR unknown comand '%s'".formatted(command));
    }
}
