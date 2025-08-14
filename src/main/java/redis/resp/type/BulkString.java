package redis.resp.type;

import java.io.ByteArrayOutputStream;

public class BulkString implements RValue {

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
            return nullBulkString();
        }

        byte[] valueBytes = value.getBytes();
        String header =
            FirstByte.BulkString + Integer.toString(valueBytes.length) + CRLF;
        byte[] headerBytes = header.getBytes();

        ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        try {
            out.write(headerBytes);
            out.write(valueBytes);
            out.write(CRLF.getBytes());
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }

    private byte[] nullBulkString() {
        return (FirstByte.BulkString + "-1" + CRLF).getBytes();
    }
}
