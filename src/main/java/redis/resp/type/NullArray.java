package redis.resp.type;

public class NullArray implements RValue {

    public static final NullArray INSTANCE = new NullArray();

    private NullArray() {}

    @Override
    public byte[] serialize() {
        return (FirstByte.Array + "-1" + CRLF).getBytes();
    }
}
