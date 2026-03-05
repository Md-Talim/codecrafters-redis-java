package redis.resp.type;

import java.util.Base64;

public class EmptyRDBFile implements RValue {

    private static final byte[] SERIALIZED;

    static {
        String emptyRDBBase64 =
            "UkVESVMwMDEx+glyZWRpcy12ZXIFNy4yLjD6CnJlZGlzLWJpdHPAQPoFY3RpbWXCbQi8ZfoIdXNlZC1tZW3CsMQQAPoIYW9mLWJhc2XAAP/wbjv+wP9aog==";
        byte[] emptyRDBData = Base64.getDecoder().decode(emptyRDBBase64);
        byte[] header = ("$" + emptyRDBData.length + "\r\n").getBytes();

        SERIALIZED = new byte[header.length + emptyRDBData.length];
        System.arraycopy(header, 0, SERIALIZED, 0, header.length);
        System.arraycopy(
            emptyRDBData,
            0,
            SERIALIZED,
            header.length,
            emptyRDBData.length
        );
    }

    @Override
    public byte[] serialize() {
        return SERIALIZED;
    }
}
