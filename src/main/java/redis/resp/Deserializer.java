package redis.resp;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import redis.resp.type.BulkString;
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
            case RValue.STAR -> readArray();
            case RValue.PLUS -> readSimpleString();
            case RValue.DOLLAR -> readBulkString();
            default -> readInlineCommand(firstByte);
        };
    }

    public byte[] readRDB() throws IOException {
        int firstByte = inputStream.read();
        if (firstByte != RValue.DOLLAR) {
            throw new IOException("Expected bulk string for RDB file");
        }
        int length = parseUnsignedInt();
        return inputStream.readNBytes(length);
    }

    private RArray readInlineCommand(int firstByte) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append((char) firstByte);

        int b;
        while ((b = inputStream.read()) != -1) {
            if (b == '\r') {
                consumeLF();
                break;
            }
            sb.append((char) b);
        }

        String[] parts = sb.toString().split(" ");
        List<RValue> args = new ArrayList<>();
        for (String part : parts) {
            args.add(new BulkString(part));
        }

        return new RArray(args);
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
        consumeCRLF();

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
                consumeLF();
                break;
            }
            sb.append((char) b);
        }
        return sb.toString();
    }

    private void consumeCRLF() throws IOException {
        int cr = inputStream.read();
        if (cr != '\r') {
            throw new IOException("Expected \\r, got " + (cr == -1 ? "EOF" : "'" + (char) cr + "'"));
        }

        consumeLF();
    }

    private void consumeLF() throws IOException {
        int lf = inputStream.read();
        if (lf != '\n') {
            throw new IOException("Expected \\n after \\r, got " + (lf == -1 ? "EOF" : "'" + (char) lf + "'"));
        }
    }
}
