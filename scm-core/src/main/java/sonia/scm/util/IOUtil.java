/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.util;

//~--- JDK imports ------------------------------------------------------------

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sebastian Sdorra
 */
public class IOUtil
{

  /** Field description */
  private static final Logger logger = Logger.getLogger(IOUtil.class.getName());

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
        logger.log(Level.SEVERE, null, ex);
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
        logger.log(Level.SEVERE, null, ex);
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
