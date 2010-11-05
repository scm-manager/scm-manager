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



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 *
 * @author Sebastian Sdorra
 */
public class IOUtil
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(IOUtil.class.getName());

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param closeable
   */
  public static void close(Closeable closeable)
  {
    if (closeable != null)
    {
      try
      {
        closeable.close();
      }
      catch (IOException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param reader
   * @param writer
   *
   * @throws IOException
   */
  public static void copy(Reader reader, Writer writer) throws IOException
  {
    char[] buffer = new char[0xFFFF];

    for (int len; (len = reader.read(buffer)) != -1; )
    {
      writer.write(buffer, 0, len);
    }
  }

  /**
   * Method description
   *
   *
   * @param in
   * @param out
   *
   * @throws IOException
   */
  public static void copy(InputStream in, OutputStream out) throws IOException
  {
    byte[] buffer = new byte[0xFFFF];

    for (int len; (len = in.read(buffer)) != -1; )
    {
      out.write(buffer, 0, len);
    }
  }

  /**
   * Method description
   *
   *
   * @param reader
   * @param writer
   */
  public static void copyThread(Reader reader, Writer writer)
  {
    new Thread(new IOCopyThread(reader, writer)).start();
  }

  /**
   *   Method description
   *
   *
   *   @param file
   *
   *   @throws IOException
   */
  public static void delete(File file) throws IOException
  {
    if (file.isDirectory())
    {
      File[] children = file.listFiles();

      if (children != null)
      {
        for (File child : children)
        {
          delete(child);
        }
      }
    }

    if (!file.delete())
    {
      throw new IOException("could not delete file ".concat(file.getPath()));
    }
  }

  /**
   * Method description
   *
   *
   * @param directory
   */
  public static void mkdirs(File directory)
  {
    if (!directory.exists() &&!directory.mkdirs())
    {
      throw new IllegalStateException(
          "could not create directory ".concat(directory.getPath()));
    }
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 10/09/28
   * @author         Enter your name here...
   */
  private static class IOCopyThread implements Runnable
  {

    /**
     * Constructs ...
     *
     *
     * @param reader
     * @param writer
     */
    public IOCopyThread(Reader reader, Writer writer)
    {
      this.reader = reader;
      this.writer = writer;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     */
    @Override
    public void run()
    {
      try
      {
        copy(reader, writer);
      }
      catch (IOException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
      finally
      {
        close(reader);
        close(writer);
      }
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private Reader reader;

    /** Field description */
    private Writer writer;
  }
}
