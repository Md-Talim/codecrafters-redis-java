package redis.resp.type;

public class NullArray implements RValue {

    public static final NullArray INSTANCE = new NullArray();

    private static final byte[] SERIALIZED = "*-1\r\n".getBytes();

    private NullArray() {}

    @Override
    public byte[] serialize() {
        return SERIALIZED;
    }
}
