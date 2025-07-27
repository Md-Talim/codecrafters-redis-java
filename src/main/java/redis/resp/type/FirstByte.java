package redis.resp.type;

/**
 * Centralizes RESP protocol first byte markers for various types.
 */
public class FirstByte {
    public static final char BulkString = '$';
    public static final char SimpleString = '+';
    public static final char Array = '*';
    public static final char SimpleError = '-';
    public static final char Integer = ':';
    public static final char Null = '_';
}