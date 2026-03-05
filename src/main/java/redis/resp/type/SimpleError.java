package redis.resp.type;

public class SimpleError implements RValue {

    private final String message;

    public SimpleError(String message) {
        this.message = message;
    }

    @Override
    public byte[] serialize() {
        byte[] msgBytes = message.getBytes();

        // -<message>\r\n
        int resultLen = 1 + msgBytes.length + 2;
        byte[] result = new byte[resultLen];

        result[0] = MINUS;
        System.arraycopy(msgBytes, 0, result, 1, msgBytes.length);
        result[resultLen - 2] = CR;
        result[resultLen - 1] = LF;

        return result;
    }
}
