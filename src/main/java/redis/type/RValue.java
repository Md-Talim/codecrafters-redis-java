package redis.type;

public interface RValue {
    String CRLF = "\r\n";

    byte[] serialize();
}