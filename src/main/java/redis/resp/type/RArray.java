package redis.resp.type;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class RArray implements RValue {
    private final List<RValue> items;

    public RArray(List<RValue> items) {
        this.items = items;
    }

    public List<RValue> getItems() {
        return items;
    }

    public RValue get(int index) {
        return items.get(index);
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public String getCommandName() {
        RValue firstItem = items.get(0);
        if (!(firstItem instanceof BulkString)) {
            return "";
        }

        BulkString bulkString = (BulkString) items.get(0);
        return bulkString.getValue();
    }

    public List<RValue> getArgs() {
        return items.subList(1, items.size());
    }

    @Override
    public byte[] serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(FirstByte.Array);
        try {
            baos.write(String.valueOf(items.size()).getBytes());
            baos.write(CRLF.getBytes());
            for (RValue item : items) {
                baos.write(item.serialize());
            }
        } catch (IOException e) {
            throw new RuntimeException("Error serializing array", e);
        }
        return baos.toByteArray();
    }
}