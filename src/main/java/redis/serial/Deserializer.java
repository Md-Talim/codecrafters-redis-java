package redis.serial;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import redis.type.BulkString;
import redis.type.FirstByte;
import redis.type.RArray;
import redis.type.RValue;
import redis.type.SimpleString;

public class Deserializer {
    private final InputStream inputStream;

    public Deserializer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public RValue read() throws IOException {
        var firstByte = inputStream.read();
        if (firstByte == -1) {
            return null;
        }

        return switch (firstByte) {
            case FirstByte.Array -> readArray();
            case FirstByte.SimpleString -> readSimpleString();
            case FirstByte.BulkString -> readBulkString();
            default -> throw new IllegalArgumentException("Unexpected value: " + firstByte);
        };
    }

    private RArray readArray() throws IOException {
        int count = Integer.parseUnsignedInt(readLine());
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
        int length = Integer.parseUnsignedInt(readLine());
        if (length == -1) {
            return null;
        }

        byte[] bytes = new byte[length];
        inputStream.readNBytes(bytes, 0, length);
        inputStream.read(); // consume \r
        inputStream.read(); // consume \n

        return new BulkString(new String(bytes));
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
