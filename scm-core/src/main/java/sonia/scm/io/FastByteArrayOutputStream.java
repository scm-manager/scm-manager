/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.io;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * ByteArrayOutputStream implementation that doesn't synchronize methods
 * and doesn't copy the data on toByteArray().
 *
 * @since 1.29
 * @see <a href="http://javatechniques.com/blog/faster-deep-copies-of-java-objects" target="_blank">http://javatechniques.com/blog/faster-deep-copies-of-java-objects</a>
 */
public final class FastByteArrayOutputStream extends OutputStream
{
  private byte[] buf = null;

  private int size = 0;

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

  @Override
  public final void write(byte b[])
  {
    verifyBufferSize(size + b.length);
    System.arraycopy(b, 0, buf, size, b.length);
    size += b.length;
  }

  @Override
  public final void write(byte b[], int off, int len)
  {
    verifyBufferSize(size + len);
    System.arraycopy(b, off, buf, size, len);
    size += len;
  }

  @Override
  public final void write(int b)
  {
    verifyBufferSize(size + 1);
    buf[size++] = (byte) b;
  }

   public void reset()
  {
    size = 0;
  }


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

  
  public int getSize()
  {
    return size;
  }


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

}
