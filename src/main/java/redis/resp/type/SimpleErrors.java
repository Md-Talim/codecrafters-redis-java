package redis.resp.type;

public class SimpleErrors {

    public static final SimpleError WRONG_TYPE_OPERATION = new SimpleError(
        "WRONGTYPE Operation against a key holding the wrong kind of value"
    );
}
