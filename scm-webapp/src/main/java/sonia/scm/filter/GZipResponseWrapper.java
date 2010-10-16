/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.filter;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(GZipResponseWrapper.class);

  //~--- constructors ---------------------------------------------------------

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
    catch (IOException ex)
    {
      logger.error(ex.getMessage(), ex);
    }
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
