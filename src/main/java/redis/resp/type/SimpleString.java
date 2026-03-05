package redis.resp.type;

public class SimpleString implements RValue {

    private final String value;

    public SimpleString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public byte[] serialize() {
        byte[] valueBytes = value.getBytes();

        // +<value>\r\n
        int resultLen = 1 + valueBytes.length + 2;
        byte[] result = new byte[resultLen];

        result[0] = PLUS;
        System.arraycopy(valueBytes, 0, result, 1, valueBytes.length);
        result[resultLen - 2] = CR;
        result[resultLen - 1] = LF;

        return result;
    }
}
