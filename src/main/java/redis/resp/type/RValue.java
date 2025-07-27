package redis.resp.type;

public interface RValue {
    String CRLF = "\r\n";

    byte[] serialize();
}