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



package sonia.scm.web.proxy;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.util.AssertUtil;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.8
 */
@Singleton
public class ProxyServet extends HttpServlet
{

  /** the logger for MyProxy */
  private static final Logger logger =
    LoggerFactory.getLogger(ProxyServet.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param urlProvider
   */
  public ProxyServet(ProxyURLProvider urlProvider)
  {
    this.urlProvider = urlProvider;
  }

  /**
   * Constructs ...
   *
   *
   * @param url
   */
  public ProxyServet(URL url)
  {
    this.urlProvider = new BasicProxyURLProvider(url);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   */
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
  {
    doPost(request, response);
  }

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   */
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
  {
    BufferedInputStream webToProxyBuf = null;
    BufferedOutputStream proxyToClientBuf = null;
    HttpURLConnection con;

    try
    {
      URL url = urlProvider.getProxyURL();

      AssertUtil.assertIsNotNull(url);

      if (logger.isInfoEnabled())
      {
        logger.info("proxy request for {}", url.toString());
      }

      con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setDoInput(true);
      con.setUseCaches(true);

      for (Enumeration e = request.getHeaderNames(); e.hasMoreElements(); )
      {
        String headerName = e.nextElement().toString();

        con.setRequestProperty(headerName, request.getHeader(headerName));
      }

      con.connect();
      response.setStatus(con.getResponseCode());

      for (Iterator i = con.getHeaderFields().entrySet().iterator();
              i.hasNext(); )
      {
        Map.Entry mapEntry = (Map.Entry) i.next();

        if (mapEntry.getKey() != null)
        {
          response.setHeader(mapEntry.getKey().toString(),
                             ((List) mapEntry.getValue()).get(0).toString());
        }
      }

      webToProxyBuf = new BufferedInputStream(con.getInputStream());
      proxyToClientBuf = new BufferedOutputStream(response.getOutputStream());

      int oneByte;

      while ((oneByte = webToProxyBuf.read()) != -1)
      {
        proxyToClientBuf.write(oneByte);
      }

      con.disconnect();
    }
    catch (Exception ex)
    {
      logger.error("could not proxy request", ex);
    }
    finally
    {
      IOUtil.close(webToProxyBuf);
      IOUtil.close(proxyToClientBuf);
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ProxyURLProvider urlProvider;
}
