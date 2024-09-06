/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.util;


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


@Singleton
public class DebugServlet extends HttpServlet
{

  private static final long serialVersionUID = -1918351712617918728L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request,
    HttpServletResponse response)
    throws ServletException, IOException
  {
    processRequest(request, response);
  }


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


  private void printFooter(PrintWriter writer)
  {
    writer.append("</body></html>");
  }


  private void printHeader(PrintWriter writer)
  {
    writer.append("<html>");
    writer.append("<head><title>SCM Manager :: Debugging</title></head>");
    writer.append("<body><h1>SCM Manager :: Debugging</h1>");
  }

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


  private interface AttributeContext
  {

  
    public Enumeration<String> getNames();

    
    public Object getValue(String name);
  }
}
