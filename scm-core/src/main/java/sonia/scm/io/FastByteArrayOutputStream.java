/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.io.InputStream;
import java.io.OutputStream;

/**
 * ByteArrayOutputStream implementation that doesn't synchronize methods
 * and doesn't copy the data on toByteArray().
 *
 * @author Sebastian Sdorra
 * @since 1.29
 * @see <a href="http://javatechniques.com/blog/faster-deep-copies-of-java-objects" target="_blank">http://javatechniques.com/blog/faster-deep-copies-of-java-objects</a>
 */
public final class FastByteArrayOutputStream extends OutputStream
{

  /**
   * Constructs a stream with buffer capacity size 5K
   */
  public FastByteArrayOutputStream()
  {
    this(5 * 1024);
  }

  /**
   * Constructs a stream with the given initial size
   *
   * @param initSize
   */
  public FastByteArrayOutputStream(int initSize)
  {
    this.size = 0;
    this.buf = new byte[initSize];
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param b
   */
  @Override
  public final void write(byte b[])
  {
    verifyBufferSize(size + b.length);
    System.arraycopy(b, 0, buf, size, b.length);
    size += b.length;
  }

  /**
   * Method description
   *
   *
   * @param b
   * @param off
   * @param len
   */
  @Override
  public final void write(byte b[], int off, int len)
  {
    verifyBufferSize(size + len);
    System.arraycopy(b, off, buf, size, len);
    size += len;
  }

  /**
   * Method description
   *
   *
   * @param b
   */
  @Override
  public final void write(int b)
  {
    verifyBufferSize(size + 1);
    buf[size++] = (byte) b;
  }

  /**
   * Method description
   *
   */
  public void reset()
  {
    size = 0;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the byte array containing the written data. Note that this
   * array will almost always be larger than the amount of data actually
   * written.
   *
   * @return
   */
  public byte[] getByteArray()
  {
    return buf;
  }

  /**
   * Returns a ByteArrayInputStream for reading back the written data
   *
   * @return
   */
  public InputStream getInputStream()
  {
    return new FastByteArrayInputStream(buf, size);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getSize()
  {
    return size;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Ensures that we have a large enough buffer for the given size.
   *
   * @param sz
   */
  private void verifyBufferSize(int sz)
  {
    if (sz > buf.length)
    {
      byte[] old = buf;

      buf = new byte[Math.max(sz, 2 * buf.length)];
      System.arraycopy(old, 0, buf, 0, old.length);
      old = null;
    }
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * Buffer and size
   */
  private byte[] buf = null;

  /** Field description */
  private int size = 0;
}
