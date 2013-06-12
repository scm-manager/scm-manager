/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.io.InputStream;

/**
 * ByteArrayInputStream implementation that does not synchronize methods.
 *
 * @author Sebastian Sdorra
 * @since 1.29
 * @see <a href="http://javatechniques.com/blog/faster-deep-copies-of-java-objects" target="_blank">http://javatechniques.com/blog/faster-deep-copies-of-java-objects</a>
 */
public final class FastByteArrayInputStream extends InputStream
{

  /**
   * Constructs ...
   *
   *
   * @param buf
   * @param count
   */
  public FastByteArrayInputStream(byte[] buf, int count)
  {
    this.buf = buf;
    this.count = count;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public final int available()
  {
    return count - pos;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public final int read()
  {
    return (pos < count)
      ? (buf[pos++] & 0xff)
      : -1;
  }

  /**
   * Method description
   *
   *
   * @param b
   * @param off
   * @param len
   *
   * @return
   */
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

  /**
   * Method description
   *
   *
   * @param n
   *
   * @return
   */
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

  //~--- fields ---------------------------------------------------------------

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
}
