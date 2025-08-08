package redis.resp.type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class EmptyRDBFile implements RValue {

    @Override
    public byte[] serialize() {
        String emptyRDBBase64 =
            "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";
        byte[] emptyRDBData = Base64.getDecoder().decode(emptyRDBBase64);

        var rdbHeader = "$%d%s".formatted(emptyRDBData.length, CRLF);

        try (var baos = new ByteArrayOutputStream()) {
            baos.write(rdbHeader.getBytes());
            baos.write(emptyRDBData);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
