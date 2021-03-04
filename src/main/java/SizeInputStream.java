import java.io.IOException;
import java.io.InputStream;

public class SizeInputStream extends InputStream {
    // The InputStream to read bytes from
    private InputStream in = null;

    // The number of bytes that can be read from the InputStream
    private int size = 0;

    // The number of bytes that have been read from the InputStream
    private int bytesRead = 0;

    public SizeInputStream(InputStream in, int size) {
        this.in = in;
        this.size = size;
    }

    public int available() {
        return (size - bytesRead);
    }

    public int read() throws IOException {
        int b = in.read();
        if (b != -1)
        {
            bytesRead++;
        }
        return b;
    }

    public int read(byte[] b) throws IOException {
        int read = in.read(b);
        bytesRead += read;
        return read;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        int read = in.read(b, off, len);
        bytesRead += read;
        return read;
    }
}