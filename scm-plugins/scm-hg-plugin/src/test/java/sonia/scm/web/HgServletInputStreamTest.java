/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.web;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import org.junit.Test;

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

    @Override
    public boolean isFinished() {
      return false;
    }

    @Override
    public boolean isReady() {
      return false;
    }

    @Override
    public void setReadListener(ReadListener readListener) {
    }
  }

}
