/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.filter;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class GZipResponseStream extends ServletOutputStream
{

  /**
   * Constructs ...
   *
   *
   * @param response
   *
   * @throws IOException
   */
  public GZipResponseStream(HttpServletResponse response) throws IOException
  {
    super();
    closed = false;
    this.response = response;
    this.output = response.getOutputStream();
    baos = new ByteArrayOutputStream();
    gzipstream = new GZIPOutputStream(baos);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {
    if (closed)
    {
      throw new IOException("This output stream has already been closed");
    }

    gzipstream.finish();

    byte[] bytes = baos.toByteArray();

    response.addHeader("Content-Length", Integer.toString(bytes.length));
    response.addHeader("Content-Encoding", "gzip");
    output.write(bytes);
    output.flush();
    output.close();
    closed = true;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean closed()
  {
    return (this.closed);
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void flush() throws IOException
  {
    if (closed)
    {
      throw new IOException("Cannot flush a closed output stream");
    }

    gzipstream.flush();
  }

  /**
   * Method description
   *
   */
  public void reset()
  {

    // noop
  }

  /**
   * Method description
   *
   *
   * @param b
   *
   * @throws IOException
   */
  @Override
  public void write(int b) throws IOException
  {
    if (closed)
    {
      throw new IOException("Cannot write to a closed output stream");
    }

    gzipstream.write((byte) b);
  }

  /**
   * Method description
   *
   *
   * @param b
   *
   * @throws IOException
   */
  @Override
  public void write(byte b[]) throws IOException
  {
    write(b, 0, b.length);
  }

  /**
   * Method description
   *
   *
   * @param b
   * @param off
   * @param len
   *
   * @throws IOException
   */
  @Override
  public void write(byte b[], int off, int len) throws IOException
  {
    if (closed)
    {
      throw new IOException("Cannot write to a closed output stream");
    }

    gzipstream.write(b, off, len);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected ByteArrayOutputStream baos = null;

  /** Field description */
  protected GZIPOutputStream gzipstream = null;

  /** Field description */
  protected boolean closed = false;

  /** Field description */
  protected ServletOutputStream output = null;

  /** Field description */
  protected HttpServletResponse response = null;
}
