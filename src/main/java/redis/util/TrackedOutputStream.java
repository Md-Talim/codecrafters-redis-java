package redis.util;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class TrackedOutputStream extends OutputStream {

    private final BufferedOutputStream delegate;
    private long written;

    public TrackedOutputStream(OutputStream outputStream) {
        this.delegate = new BufferedOutputStream(outputStream, 8192);
    }

    @Override
    public void write(int b) throws IOException {
        delegate.write(b);
        ++written;
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        delegate.write(b, off, len);
        written += len;
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    public void begin() {
        written = 0;
    }

    public long count() {
        return written;
    }
}
