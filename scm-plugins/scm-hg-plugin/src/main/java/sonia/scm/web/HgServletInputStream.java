package sonia.scm.web;

import com.google.common.base.Preconditions;

import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * HgServletInputStream is a wrapper around the original {@link ServletInputStream} and provides some extra
 * functionality to support the mercurial client.
 */
public class HgServletInputStream extends ServletInputStream {

  private final ServletInputStream original;
  private ByteArrayInputStream captured;

  HgServletInputStream(ServletInputStream original) {
    this.original = original;
  }

  /**
   * Reads the given amount of bytes from the stream and captures them, if the {@link #read()} methods is called the
   * captured bytes are returned before the rest of the stream.
   *
   * @param size amount of bytes to read
   *
   * @return byte array
   *
   * @throws IOException if the method is called twice
   */
  public byte[] readAndCapture(int size) throws IOException {
    Preconditions.checkState(captured == null, "readAndCapture can only be called once per request");

    // TODO should we enforce a limit? to prevent OOM?
    byte[] bytes = new byte[size];
    original.read(bytes);
    captured = new ByteArrayInputStream(bytes);

    return bytes;
  }

  @Override
  public int read() throws IOException {
    if (captured != null && captured.available() > 0) {
       return captured.read();
    }
    return original.read();
  }

  @Override
  public void close() throws IOException {
    original.close();
  }
}
