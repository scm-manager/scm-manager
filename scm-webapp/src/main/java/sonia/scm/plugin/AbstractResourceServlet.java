/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractResourceServlet extends HttpServlet
{

  /** Field description */
  public static final String DEFAULT_CHARSET = "UTF-8";

  /** Field description */
  private static final long serialVersionUID = -7703282364120349053L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stream
   *
   * @throws IOException
   * @throws ServletException
   */
  protected abstract void appendResources(OutputStream stream)
          throws ServletException, IOException;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected abstract String getContentType();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws ServletException
   */
  @Override
  public void init() throws ServletException
  {
    ByteArrayOutputStream output = new ByteArrayOutputStream();

    try
    {
      appendResources(output);
    }
    catch (IOException ex)
    {
      throw new ServletException(ex);
    }
    finally
    {
      Util.close(output);
    }

    this.content = output.toByteArray();
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void doPost(HttpServletRequest request,
                        HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  protected String getCharacterEncoding()
  {
    return DEFAULT_CHARSET;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   *
   * @throws IOException
   * @throws ServletException
   */
  private void processRequest(HttpServletRequest request,
                              HttpServletResponse response)
          throws ServletException, IOException
  {
    response.setCharacterEncoding(getCharacterEncoding());
    response.setContentType(getContentType());
    response.setContentLength(content.length);

    OutputStream output = response.getOutputStream();

    try
    {
      output.write(content);
    }
    finally
    {
      Util.close(output);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private byte[] content;
}
