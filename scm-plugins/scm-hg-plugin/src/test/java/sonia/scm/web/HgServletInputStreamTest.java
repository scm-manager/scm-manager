package sonia.scm.web;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.junit.Test;

import javax.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class HgServletInputStreamTest {

  @Test
  public void testReadAndCapture() throws IOException {
    SampleServletInputStream original = new SampleServletInputStream("trillian.mcmillian@hitchhiker.com");
    HgServletInputStream hgServletInputStream = new HgServletInputStream(original);

    byte[] prefix = hgServletInputStream.readAndCapture(8);
    assertEquals("trillian", new String(prefix, Charsets.US_ASCII));

    byte[] wholeBytes = ByteStreams.toByteArray(hgServletInputStream);
    assertEquals("trillian.mcmillian@hitchhiker.com", new String(wholeBytes, Charsets.US_ASCII));
  }

  @Test(expected = IllegalStateException.class)
  public void testReadAndCaptureCalledTwice() throws IOException {
    SampleServletInputStream original = new SampleServletInputStream("trillian.mcmillian@hitchhiker.com");
    HgServletInputStream hgServletInputStream = new HgServletInputStream(original);

    hgServletInputStream.readAndCapture(1);
    hgServletInputStream.readAndCapture(1);
  }

  private static class SampleServletInputStream extends ServletInputStream {

    private ByteArrayInputStream input;

    private SampleServletInputStream(String data) {
      input = new ByteArrayInputStream(data.getBytes());
    }

    @Override
    public int read() {
      return input.read();
    }
  }

}
