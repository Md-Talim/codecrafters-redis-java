package redis.resp.type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RInteger implements RValue {

    private final int integer;

    public RInteger(int value) {
        this.integer = value;
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(FirstByte.Integer);
        try {
            baos.write(String.valueOf(integer).getBytes());
            baos.write(CRLF.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error serializing integer", e);
        }
        return baos.toByteArray();
    }
}
