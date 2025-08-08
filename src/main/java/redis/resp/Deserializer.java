package redis.resp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import redis.resp.type.BulkString;
import redis.resp.type.FirstByte;
import redis.resp.type.RArray;
import redis.resp.type.RValue;
import redis.resp.type.SimpleString;

public class Deserializer {

    private final InputStream inputStream;

    public Deserializer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public RValue read() throws IOException {
        return read(false);
    }

    public RValue read(boolean likelyBlob) throws IOException {
        var firstByte = inputStream.read();
        if (firstByte == -1) {
            return null;
        }

        return switch (firstByte) {
            case FirstByte.Array -> readArray();
            case FirstByte.SimpleString -> readSimpleString();
            case FirstByte.BulkString -> readBulkString();
            default -> throw new IllegalArgumentException(
                "Unexpected value: " + firstByte
            );
        };
    }

    public byte[] readRDB() throws IOException {
        int firtByte = inputStream.read();
        if (firtByte != FirstByte.BulkString) {
            throw new IOException("Expected bulk string for RDB file");
        }
        int length = parseUnsignedInt();
        return inputStream.readNBytes(length);
    }

    private RArray readArray() throws IOException {
        int count = parseUnsignedInt();
        List<RValue> array = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            RValue value = read();
            if (value == null) {
                return null;
            }
            array.add(value);
        }
        return new RArray(array);
    }

    private SimpleString readSimpleString() throws IOException {
        return new SimpleString(readLine());
    }

    private BulkString readBulkString() throws IOException {
        int length = parseUnsignedInt();
        if (length == -1) {
            return null;
        }

        byte[] bytes = new byte[length];
        inputStream.readNBytes(bytes, 0, length);
        inputStream.read(); // consume \r
        inputStream.read(); // consume \n

        return new BulkString(new String(bytes));
    }

    private int parseUnsignedInt() throws IOException {
        String line = readLine();
        if ("-1".equals(line)) {
            return -1;
        }
        return Integer.parseInt(line);
    }

    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        int b;
        while ((b = inputStream.read()) != -1) {
            if (b == '\r') {
                inputStream.read(); // consume \n
                break;
            }
            sb.append((char) b);
        }
        return sb.toString();
    }
}
