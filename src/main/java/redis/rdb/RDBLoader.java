package redis.rdb;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import redis.store.CacheEntry;
import redis.store.Storage;

public class RDBLoader {

    public static final String RDB_HEADER = "REDIS0011";
    public static final byte OP_AUX = (byte) 0xFA;
    public static final byte OP_SELECTDB = (byte) 0xFE;
    public static final byte OP_EOF = (byte) 0xFF;
    public static final byte OP_RESIZE_DB = (byte) 0xFB;
    public static final byte OP_EXPIRETIME = (byte) 0xFD;
    public static final byte OP_EXPIRETIME_MS = (byte) 0xFC;
    public static final byte VALUE_TYPE_STRING = (byte) 0x00;

    private final DataInputStream in;
    private final Storage storage;

    public RDBLoader(DataInputStream inputStream, Storage storage) {
        this.in = inputStream;
        this.storage = storage;
    }

    public void load() throws IOException {
        checkHeader();

        while (true) {
            int op = in.read();
            if (op == -1) {
                break;
            }
            byte opcode = (byte) op;

            if (opcode == OP_AUX) {
                skipString();
                skipString();
            } else if (opcode == OP_SELECTDB) {
                readSize(); // db index
                int nextByte = in.read();
                if ((byte) nextByte == OP_RESIZE_DB) {
                    readSize(); // hash table size
                    readSize(); // expires hash table size
                } else {
                    in.skip(-1); // push back
                }

                // Read key-value pairs
                while (true) {
                    int kvOp = in.read();
                    if (
                        kvOp == -1 ||
                        kvOp == OP_SELECTDB ||
                        kvOp == OP_EOF ||
                        kvOp == OP_AUX
                    ) {
                        if (kvOp != -1) {
                            in.skip(-1);
                        }
                        break;
                    }

                    byte valueType = (byte) kvOp;
                    Long expiresAt = null;

                    if (valueType == OP_EXPIRETIME) {
                        long ts = readUint32();
                        expiresAt = ts * 1000;
                        valueType = (byte) in.read();
                    } else if (valueType == OP_EXPIRETIME_MS) {
                        long ts = readUint64();
                        expiresAt = ts;
                        valueType = (byte) in.read();
                    }

                    if (valueType != VALUE_TYPE_STRING) {
                        throw new IOException(
                            "Unsupported value type: " + valueType
                        );
                    }

                    String key = readString();
                    String value = readString();

                    if (expiresAt != null) {
                        long millisecondsFromNow =
                            expiresAt - System.currentTimeMillis();
                        if (millisecondsFromNow > 0) {
                            storage.put(
                                key,
                                CacheEntry.expiringIn(
                                    value,
                                    millisecondsFromNow
                                )
                            );
                        }
                    } else {
                        storage.set(key, value);
                    }
                }
            } else if (opcode == OP_EOF) {
                break;
            } else {
                throw new IOException(
                    String.format("Unexpected opcode: 0x%02X", opcode)
                );
            }
        }
    }

    private void checkHeader() throws IOException {
        byte[] header = new byte[RDB_HEADER.length()];
        if (
            in.read(header) != header.length ||
            !new String(header).equals(RDB_HEADER)
        ) {
            throw new IOException("Invalid RDB header");
        }
    }

    private void skipString() throws IOException {
        readString(); // ignore the result
    }

    private long readSize() throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        int encodingType = (b & 0xC0) >> 6;
        if (encodingType == 0) {
            return b & 0x3F;
        } else if (encodingType == 1) {
            int next = in.read();
            if (next == -1) {
                throw new EOFException();
            }
            return ((b & 0x3F) << 8) | next;
        } else if (encodingType == 2) {
            return readUint32BigEndian();
        } else {
            throw new IOException(
                "Special string encoding not supported in size context"
            );
        }
    }

    private String readString() throws IOException {
        long size = readStringSize();
        if ((size & 0xC0) == 0xC0) {
            if (size == 0xC0) {
                int v = in.read();
                return Integer.toString(v);
            } else if (size == 0xC1) {
                int b1 = in.read();
                int b2 = in.read();
                return Integer.toString((b2 << 8) | b1);
            } else if (size == 0xC2) {
                long val = readUint32();
                return Long.toString(val);
            } else {
                throw new IOException("Unsupported string encoding: " + size);
            }
        }

        if (size == 0) {
            return "";
        }

        byte[] buf = new byte[(int) size];
        int read = in.read(buf);
        if (read != size) {
            throw new IOException("Unexpected EOF reading string");
        }
        return new String(buf);
    }

    private long readStringSize() throws IOException {
        int b = in.read();
        if (b == -1) {
            throw new EOFException();
        }
        int encodingType = (b & 0xC0) >> 6;
        if (encodingType == 0) {
            return b & 0x3F;
        } else if (encodingType == 1) {
            int next = in.read();
            if (next == -1) {
                throw new EOFException();
            }
            return ((b & 0x3F) << 8) | next;
        } else if (encodingType == 2) {
            return readUint32BigEndian();
        } else {
            return b;
        }
    }

    private long readUint32() throws IOException {
        byte[] bytes = new byte[4];
        if (in.read(bytes) != 4) {
            throw new EOFException();
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt() & 0xFFFFFFFFL;
    }

    private long readUint32BigEndian() throws IOException {
        byte[] bytes = new byte[4];
        if (in.read(bytes) != 4) {
            throw new EOFException();
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
        return bb.getInt() & 0xFFFFFFFFL;
    }

    private long readUint64() throws IOException {
        byte[] bytes = new byte[8];
        if (in.read(bytes) != 8) {
            throw new EOFException();
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
        return bb.getLong();
    }

    public static class RDBValue {

        public final String value;
        public final Long expiresAt;

        public RDBValue(String value, Long expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }

    public static void load(Path path, Storage storage) {
        try (
            var fileInputStream = Files.newInputStream(path);
            var dataInputStream = new DataInputStream(fileInputStream)
        ) {
            var loader = new RDBLoader(dataInputStream, storage);
            loader.load();
        } catch (IOException e) {
            System.err.println("Error parsing RDB file");
        }
    }

    public void close() throws IOException {
        in.close();
    }
}
