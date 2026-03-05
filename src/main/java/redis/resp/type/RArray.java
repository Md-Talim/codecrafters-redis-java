package redis.resp.type;

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

    public void addAll(List<RValue> newItems) {
        items.addAll(newItems);
    }

    public void addAll(int index, List<RValue> newItems) {
        items.addAll(index, newItems);
    }

    public RValue remove(int index) {
        return items.remove(index);
    }

    public int size() {
        return items.size();
    }

    public List<RValue> range(int start, int stop) {
        return items.subList(start, stop + 1);
    }

    @Override
    public byte[] serialize() {
        byte[][] parts = new byte[items.size()][];

        int totalLen = 0;
        for (int i = 0; i < items.size(); i++) {
            parts[i] = items.get(i).serialize();
            totalLen += parts[i].length;
        }

        byte[] sizeBytes = Integer.toString(items.size()).getBytes();

        // *<count>\r\n<parts...>
        int resultLen = 1 + sizeBytes.length + 2 + totalLen;
        byte[] result = new byte[resultLen];

        int pos = 0;
        result[pos++] = STAR;
        System.arraycopy(sizeBytes, 0, result, pos, sizeBytes.length);
        pos += sizeBytes.length;
        result[pos++] = CR;
        result[pos++] = LF;

        for (byte[] part : parts) {
            System.arraycopy(part, 0, result, pos, part.length);
            pos += part.length;
        }

        return result;
    }
}
