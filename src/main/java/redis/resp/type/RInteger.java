package redis.resp.type;

public class RInteger implements RValue {

    private final String value;

    public RInteger(Object value) {
        this.value = String.valueOf(value);
    }

    @Override
    public byte[] serialize() {
        byte[] valueBytes = value.getBytes();

        // :<value>\r\n
        int resultLen = 1 + valueBytes.length + 2;
        byte[] result = new byte[resultLen];

        result[0] = COLON;
        System.arraycopy(valueBytes, 0, result, 1, valueBytes.length);
        result[resultLen - 2] = CR;
        result[resultLen - 1] = LF;

        return result;
    }

    @Override
    public String toString() {
        return value;
    }

    public String value() {
        return value;
    }
}
