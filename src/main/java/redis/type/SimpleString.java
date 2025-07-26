package redis.type;

public class SimpleString implements RValue {
    private final String value;

    public SimpleString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public byte[] serialize() {
        return (FirstByte.SimpleString + value + CRLF).getBytes();
    }
}