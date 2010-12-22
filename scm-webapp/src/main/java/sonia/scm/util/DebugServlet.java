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
