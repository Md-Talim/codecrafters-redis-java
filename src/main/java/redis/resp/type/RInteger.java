package redis.resp.type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RInteger implements RValue {

    private final String value;

    public RInteger(Object value) {
        this.value = String.valueOf(value);
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(FirstByte.Integer);
        try {
            baos.write(value.getBytes());
            baos.write(CRLF.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error serializing integer", e);
        }
        return baos.toByteArray();
    }

    @Override
    public String toString() {
        return value;
    }

    public String value() {
        return value;
    }
}
