package redis.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TrackedInputStream extends InputStream {

    private final BufferedInputStream delegate;
    private long read;

    public TrackedInputStream(InputStream inputStream) {
        this.delegate = new BufferedInputStream(inputStream, 8192);
    }

    @Override
    public int available() throws IOException {
        return delegate.available();
    }

    @Override
    public int read() throws IOException {
        int value = delegate.read();
        if (value != -1) {
            ++read;
        }

        return value;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int bytesRead = delegate.read(b, off, len);
        if (bytesRead > 0) {
            read += bytesRead;
        }
        return bytesRead;
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        byte[] bytes = delegate.readNBytes(len);
        read += bytes.length;
        return bytes;
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        int bytesRead = delegate.readNBytes(b, off, len);
        read += bytesRead;
        return bytesRead;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public void begin() {
        read = 0;
    }

    public long count() {
        return read;
    }
}
