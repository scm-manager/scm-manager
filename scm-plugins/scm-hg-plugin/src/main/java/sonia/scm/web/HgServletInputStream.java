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

import com.google.common.base.Preconditions;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;

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

  @Override
  public boolean isFinished() {
    return original.isFinished();
  }

  @Override
  public boolean isReady() {
    return original.isReady();
  }

  @Override
  public void setReadListener(ReadListener readListener) {
    original.setReadListener(readListener);
  }
}
