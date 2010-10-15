/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class DebugServlet extends HttpServlet
{

  /** Field description */
  private static final long serialVersionUID = -1918351712617918728L;

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

  /**
   * Method description
   *
   *
   * @param writer
   * @param context
   */
  private void appendAttributes(PrintWriter writer, AttributeContext context)
  {
    writer.append("<ul>");

    Enumeration<?> enm = context.getNames();

    while (enm.hasMoreElements())
    {
      String key = (String) enm.nextElement();

      writer.append("<li>").append(key).append(" = ");

      Object value = context.getValue(key);

      writer.append("(").append(value.getClass().toString()).append(") ");
      writer.append(value.toString()).append("</li>");
    }

    writer.append("</ul>");
  }

  /**
   * Method description
   *
   *
   * @param writer
   */
  private void appendContextAttributes(PrintWriter writer)
  {
    writer.append("<h2>ServletContext Attributes</h2>");

    final ServletContext context = getServletContext();

    appendAttributes(writer, new AttributeContext()
    {
      @Override
      public Enumeration<String> getNames()
      {
        return context.getAttributeNames();
      }
      @Override
      public Object getValue(String name)
      {
        return context.getAttribute(name);
      }
    });
  }

  /**
   * Method description
   *
   *
   * @param writer
   * @param session
   */
  private void appendSessionAttributes(PrintWriter writer,
          final HttpSession session)
  {
    writer.append("<h2>Session Attributes</h2>");
    appendAttributes(writer, new AttributeContext()
    {
      @Override
      public Enumeration<String> getNames()
      {
        return session.getAttributeNames();
      }
      @Override
      public Object getValue(String name)
      {
        return session.getAttribute(name);
      }
    });
  }

  /**
   * Method description
   *
   *
   * @param writer
   */
  private void printFooter(PrintWriter writer)
  {
    writer.append("</body></html>");
  }

  /**
   * Method description
   *
   *
   * @param writer
   */
  private void printHeader(PrintWriter writer)
  {
    writer.append("<html>");
    writer.append("<head><title>SCM Manaer :: Debugging</title></head>");
    writer.append("<body><h1>SCM Manaer :: Debugging</h1>");
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
  private void processRequest(HttpServletRequest request,
                              HttpServletResponse response)
          throws ServletException, IOException
  {
    PrintWriter writer = null;

    try
    {
      response.setContentType("text/html");
      writer = response.getWriter();
      printHeader(writer);
      appendContextAttributes(writer);

      HttpSession session = request.getSession();

      appendSessionAttributes(writer, session);
      printFooter(writer);
    }
    finally
    {
      writer.close();
    }
  }

  //~--- inner interfaces -----------------------------------------------------

  /**
   * Interface description
   *
   *
   * @version        Enter version here..., 10/10/15
   * @author         Enter your name here...
   */
  private interface AttributeContext
  {

    /**
     * Method description
     *
     *
     * @return
     */
    public Enumeration<String> getNames();

    /**
     * Method description
     *
     *
     * @param name
     *
     * @return
     */
    public Object getValue(String name);
  }
}
