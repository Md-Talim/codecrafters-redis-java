package redis.resp.type;

public interface RValue {
    /**
     * First byte for BulkString
     */
    static final byte DOLLAR = '$';

    /**
     * First byte for Array
     */
    static final byte STAR = '*';

    /**
     * First byte for SimpleString
     */
    static final byte PLUS = '+';

    /**
     * First byte for SimpleError
     */
    static final byte MINUS = '-';

    /**
     * First byte for Integer
     */
    static final byte COLON = ':';

    static final byte CR = '\r';
    static final byte LF = '\n';

    byte[] serialize();
}
