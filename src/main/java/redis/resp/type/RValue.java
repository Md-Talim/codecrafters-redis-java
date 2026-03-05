package redis.resp.type;

public interface RValue {
    static final byte DOLLAR = '$';
    static final byte STAR = '*';
    static final byte PLUS = '+';
    static final byte MINUS = '-';
    static final byte COLON = ':';
    static final byte CR = '\r';
    static final byte LF = '\n';

    byte[] serialize();
}
