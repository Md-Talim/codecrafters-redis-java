package redis.resp.type;

public class SimpleError implements RValue {

    private final String message;

    public SimpleError(String message) {
        this.message = message;
    }

    @Override
    public byte[] serialize() {
        return (FirstByte.SimpleError + message + CRLF).getBytes();
    }

    public static SimpleError wrongArguments(String command) {
        return new SimpleError(
            "wrong number of arguments for '%s' command".formatted(command)
        );
    }
}
