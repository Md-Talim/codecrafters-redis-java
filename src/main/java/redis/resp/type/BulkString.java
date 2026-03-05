package redis.resp.type;

public class BulkString implements RValue {

    private static final byte[] NULL_BULK_STRING = "$-1\r\n".getBytes();

    private final String value;

    public BulkString(String value) {
        this.value = value;
    }

    public BulkString(int value) {
        this.value = String.valueOf(value);
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
        if (value == null) {
            return NULL_BULK_STRING;
        }

        byte[] valueBytes = value.getBytes();
        byte[] lengthBytes = Integer.toString(valueBytes.length).getBytes();

        // $<length>\r\n<value>\r\n
        int resultLength = 1 + lengthBytes.length + 2 + valueBytes.length + 2;
        byte[] result = new byte[resultLength];

        int pos = 0;
        result[pos++] = DOLLAR;
        System.arraycopy(lengthBytes, 0, result, pos, lengthBytes.length);
        pos += lengthBytes.length;
        result[pos++] = CR;
        result[pos++] = LF;
        System.arraycopy(valueBytes, 0, result, pos, valueBytes.length);
        pos += valueBytes.length;
        result[pos++] = CR;
        result[pos++] = LF;

        return result;
    }
}
