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
    
package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

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
  @SuppressWarnings("unchecked")
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
  @SuppressWarnings("unchecked")
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
    writer.append("<head><title>SCM Manager :: Debugging</title></head>");
    writer.append("<body><h1>SCM Manager :: Debugging</h1>");
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

      HttpSession session = request.getSession(false);

      if (session != null)
      {
        appendSessionAttributes(writer, session);
      }

      printFooter(writer);
    }
    finally
    {
      IOUtil.close(writer);
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
