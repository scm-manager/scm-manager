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
