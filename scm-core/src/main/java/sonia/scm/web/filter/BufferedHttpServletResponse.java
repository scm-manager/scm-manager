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

package sonia.scm.web.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class BufferedHttpServletResponse extends HttpServletResponseWrapper
{
  private int contentLength = -1;

  private ByteArrayPrintWriter pw = null;

  private Set<Cookie> cookies = new HashSet<>();

  private int statusCode = HttpServletResponse.SC_OK;

  private Map<String, String> headers = new LinkedHashMap<>();

  private String statusMessage;

  private static final Logger LOG = LoggerFactory.getLogger(BufferedHttpServletResponse.class);

  public BufferedHttpServletResponse(HttpServletResponse response,
                                     boolean logBody)
  {
    super(response);

    if (logBody)
    {
      pw = new ByteArrayPrintWriter();
    }
  }



  @Override
  public void addCookie(Cookie cookie)
  {
    cookies.add(cookie);
    super.addCookie(cookie);
  }


  @Override
  public void addDateHeader(String name, long date)
  {
    headers.put(name, new Date(date).toString());
    super.addDateHeader(name, date);
  }


  @Override
  public void addHeader(String name, String value)
  {
    headers.put(name, value);
    super.addHeader(name, value);
  }


  @Override
  public void addIntHeader(String name, int value)
  {
    headers.put(name, Integer.toString(value));
    super.addIntHeader(name, value);
  }

  
  @Override
  public void sendError(int sc) throws IOException
  {
    this.statusCode = sc;
    super.sendError(sc);
  }


  @Override
  public void sendError(int sc, String msg) throws IOException
  {
    this.statusCode = sc;
    this.statusMessage = msg;
    super.sendError(sc, msg);
  }


  
  public byte[] getContentBuffer()
  {
    byte[] content = null;

    if (pw != null)
    {
      content = pw.toByteArray();
    }

    return content;
  }

  
  public int getContentLength()
  {
    return contentLength;
  }

  
  public Set<Cookie> getCookies()
  {
    return cookies;
  }

  
  public Map<String, String> getHeaders()
  {
    return headers;
  }


  @Override
  public ServletOutputStream getOutputStream() throws IOException
  {
    return (pw != null)
           ? pw.getStream()
           : super.getOutputStream();
  }

  
  public int getStatusCode()
  {
    return statusCode;
  }

  
  public String getStatusMessage()
  {
    return statusMessage;
  }


  @Override
  public PrintWriter getWriter() throws IOException
  {
    return (pw != null)
           ? pw.getWriter()
           : super.getWriter();
  }



  @Override
  public void setContentLength(int len)
  {
    this.contentLength = len;
    super.setContentLength(len);
  }


  @Override
  public void setDateHeader(String name, long date)
  {
    headers.put(name, new Date(date).toString());
    super.setDateHeader(name, date);
  }


  @Override
  public void setHeader(String name, String value)
  {
    headers.put(name, value);
    super.setHeader(name, value);
  }


  @Override
  public void setIntHeader(String name, int value)
  {
    headers.put(name, Integer.toString(value));
    super.setIntHeader(name, value);
  }


  @Override
  public void setStatus(int sc)
  {
    this.statusCode = sc;
    super.setStatus(sc);
  }


////  @Override
//  @SuppressWarnings("deprecation")
  //TODO What to do?
//  public void setStatus(int sc, String sm)
//  {
//    this.statusCode = sc;
//    this.statusMessage = sm;
//    super.setStatus(sc);
//  }




  private static class ByteArrayPrintWriter
  {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    private PrintWriter pw = new PrintWriter(baos);

    private ServletOutputStream sos = new ByteArrayServletStream(baos);

  
    public byte[] toByteArray()
    {
      return baos.toByteArray();
    }

    public ServletOutputStream getStream()
    {
      return sos;
    }

  
    public PrintWriter getWriter()
    {
      return pw;
    }

  }



  private static class ByteArrayServletStream extends ServletOutputStream
  {
    private ByteArrayOutputStream baos;

    ByteArrayServletStream(ByteArrayOutputStream baos)
    {
      this.baos = baos;
    }

    @Override
    public void write(int param) throws IOException
    {
      baos.write(param);
    }

    @Override
    public boolean isReady() {
      return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
      try {
        writeListener.onWritePossible();
      } catch (IOException e) {
        LOG.debug("could not call writeListener.onWritePossible()", e);
      }
    }

  }

}
