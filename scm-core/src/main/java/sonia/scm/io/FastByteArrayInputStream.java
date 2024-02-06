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
    
package sonia.scm.io;

import java.io.InputStream;

/**
 * ByteArrayInputStream implementation that does not synchronize methods.
 *
 * @since 1.29
 * @see <a href="http://javatechniques.com/blog/faster-deep-copies-of-java-objects" target="_blank">http://javatechniques.com/blog/faster-deep-copies-of-java-objects</a>
 */
public final class FastByteArrayInputStream extends InputStream
{
    /**
   * Our byte buffer
   */
  private byte[] buf = null;

  /**
   * Number of bytes that we can read from the buffer
   */
  private int count = 0;

  /**
   * Number of bytes that have been read from the buffer
   */
  private int pos = 0;

  public FastByteArrayInputStream(byte[] buf, int count)
  {
    this.buf = buf;
    this.count = count;
  }

  @Override
  public final int available()
  {
    return count - pos;
  }


  @Override
  public final int read()
  {
    return (pos < count)
      ? (buf[pos++] & 0xff)
      : -1;
  }

  @Override
  public final int read(byte[] b, int off, int len)
  {
    if (pos >= count)
    {
      return -1;
    }

    if ((pos + len) > count)
    {
      len = (count - pos);
    }

    System.arraycopy(buf, pos, b, off, len);
    pos += len;

    return len;
  }

  @Override
  public final long skip(long n)
  {
    if ((pos + n) > count)
    {
      n = count - pos;
    }

    if (n < 0)
    {
      return 0;
    }

    pos += n;

    return n;
  }

}
