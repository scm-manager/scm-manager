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
    
package sonia.scm.logging;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;

/**
 * An OutputStream that flushes out to a Logger.
 * Based on LoggingOutputStream by Jim Moore
 * 
 * @author Sebastian Sdorra
 */
public class LoggingOutputStream extends OutputStream
{

  /**
   * The default number of bytes in the buffer.
   */
  public static final int DEFAULT_BUFFER_LENGTH = 2048;

  /** Field description */
  public static final int LEVEL_DEBUG = 1;

  /** Field description */
  public static final int LEVEL_ERROR = 4;

  /** Field description */
  public static final int LEVEL_INFO = 2;

  /** Field description */
  public static final int LEVEL_TRACE = 0;

  /** Field description */
  public static final int LEVEL_WARN = 3;

  //~--- constructors ---------------------------------------------------------

  /**
   * Creates the LoggingOutputStream to flush to the given Logger.
   *
   * @param logger - the Logger to write to
   * @param level - the Level to use when writing to the Logger
   * @throws IllegalArgumentException if cat == null or priority ==
   *                                  null
   */
  public LoggingOutputStream(Logger logger, int level)
          throws IllegalArgumentException
  {
    if (logger == null)
    {
      throw new IllegalArgumentException("cat == null");
    }

    this.level = level;
    this.logger = logger;
    bufferLength = DEFAULT_BUFFER_LENGTH;
    buffer = new byte[DEFAULT_BUFFER_LENGTH];
    count = 0;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Closes this output stream and releases any system resources
   * associated with this stream. The general contract of
   * <code>close</code>
   * is that it closes the output stream. A closed stream cannot
   * perform
   * output operations and cannot be reopened.
   */
  @Override
  public void close()
  {
    flush();
    closed = true;
  }

  /**
   * Flushes this output stream and forces any buffered output bytes
   * to be written out. The general contract of <code>flush</code> is
   * that calling it is an indication that, if any bytes previously
   * written have been buffered by the implementation of the output
   * stream, such bytes should immediately be written to their
   * intended destination.
   */
  @Override
  public void flush()
  {
    if (count == 0)
    {
      return;
    }

    // don't print out blank lines; flushing from PrintStream puts
    // For linux system
    if ((count == 1) && ((char) buffer[0]) == '\n')
    {
      reset();

      return;
    }

    // For mac system
    if ((count == 1) && ((char) buffer[0]) == '\r')
    {
      reset();

      return;
    }

    // On windows system
    if ((count == 2) && (char) buffer[0] == '\r' && (char) buffer[1] == '\n')
    {
      reset();

      return;
    }

    final byte[] theBytes = new byte[count];

    System.arraycopy(buffer, 0, theBytes, 0, count);
    log(new String(theBytes));
    reset();
  }

  /**
   * Method description
   *
   *
   * @param message
   */
  public void log(String message)
  {
    switch (level)
    {
      case LEVEL_TRACE :
        logger.trace(message);

        break;

      case LEVEL_DEBUG :
        logger.debug(message);

        break;

      case LEVEL_INFO :
        logger.info(message);

        break;

      case LEVEL_WARN :
        logger.warn(message);

        break;

      case LEVEL_ERROR :
        logger.error(message);

        break;

      default :
        logger.warn(message);
    }
  }

  /**
   * Writes the specified byte to this output stream. The general
   * contract for <code>write</code> is that one byte is written
   * to the output stream. The byte to be written is the eight
   * low-order bits of the argument <code>b</code>. The 24
   * high-order bits of <code>b</code> are ignored.
   *
   * @param b the <code>byte</code> to write
   *
   * @throws IOException
   */
  @Override
  public void write(final int b) throws IOException
  {
    if (closed)
    {
      throw new IOException("The stream has been closed.");
    }

    // would this be writing past the buffer?
    if (count == bufferLength)
    {

      // grow the buffer
      final int newBufLength = bufferLength + DEFAULT_BUFFER_LENGTH;
      final byte[] newBuf = new byte[newBufLength];

      System.arraycopy(buffer, 0, newBuf, 0, bufferLength);
      buffer = newBuf;
      bufferLength = newBufLength;
    }

    buffer[count] = (byte) b;
    count++;
  }

  /**
   * Method description
   *
   */
  private void reset()
  {
    count = 0;
  }

  //~--- fields ---------------------------------------------------------------

  /**
   * The internal buffer where data is stored.
   */
  private byte[] buffer;

  /**
   * Remembers the size of the buffer for speed.
   */
  private int bufferLength;

  /** Field description */
  private boolean closed = false;

  /**
   * The number of valid bytes in the buffer. This value is always
   * in the range <tt>0</tt> through <tt>buf.length</tt>; elements
   * <tt>buf[0]</tt> through <tt>buf[count-1]</tt> contain valid
   * byte data.
   */
  private int count;

  /** Field description */
  private int level = -1;

  /** The logger to write to. */
  private Logger logger;
}
