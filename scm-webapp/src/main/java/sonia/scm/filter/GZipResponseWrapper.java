/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.filter;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 *
 * @author Sebastian Sdorra
 */
public class GZipResponseWrapper extends HttpServletResponseWrapper
{

  /**
   * Constructs ...
   *
   *
   * @param response
   */
  public GZipResponseWrapper(HttpServletResponse response)
  {
    super(response);
    origResponse = response;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public ServletOutputStream createOutputStream() throws IOException
  {
    return (new GZipResponseStream(origResponse));
  }

  /**
   * Method description
   *
   */
  public void finishResponse()
  {
    try
    {
      if (writer != null)
      {
        writer.close();
      }
      else
      {
        if (stream != null)
        {
          stream.close();
        }
      }
    }
    catch (IOException e) {}
  }

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void flushBuffer() throws IOException
  {
    stream.flush();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public ServletOutputStream getOutputStream() throws IOException
  {
    if (writer != null)
    {
      throw new IllegalStateException("getWriter() has already been called!");
    }

    if (stream == null)
    {
      stream = createOutputStream();
    }

    return (stream);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public PrintWriter getWriter() throws IOException
  {
    if (writer != null)
    {
      return (writer);
    }

    if (stream != null)
    {
      throw new IllegalStateException(
          "getOutputStream() has already been called!");
    }

    stream = createOutputStream();
    writer = new PrintWriter(new OutputStreamWriter(stream, "UTF-8"));

    return (writer);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param length
   */
  @Override
  public void setContentLength(int length) {}

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  protected HttpServletResponse origResponse = null;

  /** Field description */
  protected ServletOutputStream stream = null;

  /** Field description */
  protected PrintWriter writer = null;
}
