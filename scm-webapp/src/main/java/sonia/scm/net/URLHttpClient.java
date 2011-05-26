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



package sonia.scm.net;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class URLHttpClient implements HttpClient
{

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";

  /** Field description */
  public static final String HEADER_ACCEPT_ENCODING_VALUE = "gzip";

  /** Field description */
  public static final String HEADER_USERAGENT = "User-Agent";

  /** Field description */
  public static final String HEADER_USERAGENT_VALUE = "SCM-Manager ";

  /** Field description */
  public static final String METHOD_POST = "POST";

  /** the logger for URLHttpClient */
  private static final Logger logger =
    LoggerFactory.getLogger(URLHttpClient.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   *
   * @param context
   * @param configuration
   */
  @Inject
  public URLHttpClient(SCMContextProvider context,
                       ScmConfiguration configuration)
  {
    this.context = context;
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public HttpResponse post(String url) throws IOException
  {
    HttpURLConnection connection = (HttpURLConnection) openConnection(url);

    connection.setRequestMethod(METHOD_POST);

    return new URLHttpResponse(connection);
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param parameters
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public HttpResponse post(String url, Map<String, List<String>> parameters)
          throws IOException
  {
    HttpURLConnection connection = (HttpURLConnection) openConnection(url);

    connection.setRequestMethod(METHOD_POST);

    if (Util.isNotEmpty(parameters))
    {
      connection.setDoOutput(true);

      OutputStreamWriter writer = null;

      try
      {
        writer = new OutputStreamWriter(connection.getOutputStream());

        Iterator<Map.Entry<String, List<String>>> it =
          parameters.entrySet().iterator();

        while (it.hasNext())
        {
          Map.Entry<String, List<String>> p = it.next();
          List<String> values = p.getValue();

          if (Util.isNotEmpty(values))
          {
            String key = encode(p.getKey());

            for (String value : values)
            {
              writer.append(key).append("=").append(encode(value));
            }

            if (it.hasNext())
            {
              writer.write("&");
            }
          }
        }
      }
      finally
      {
        IOUtil.close(writer);
      }
    }

    return new URLHttpResponse(connection);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param spec
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public HttpResponse get(String spec) throws IOException
  {
    return new URLHttpResponse(openConnection(spec));
  }

  /**
   * Method description
   *
   *
   * @param url
   * @param parameters
   *
   * @return
   *
   * @throws IOException
   */
  @Override
  public HttpResponse get(String url, Map<String, List<String>> parameters)
          throws IOException
  {
    if (Util.isNotEmpty(parameters))
    {
      StringBuilder ub = new StringBuilder(url);
      boolean first = url.contains("?");

      for (Map.Entry<String, List<String>> p : parameters.entrySet())
      {
        String key = encode(p.getKey());
        List<String> values = p.getValue();

        if (Util.isNotEmpty(values))
        {
          for (String value : values)
          {
            if (first)
            {
              ub.append("?");
              first = false;
            }
            else
            {
              ub.append("&");
            }

            ub.append(key).append("=").append(encode(value));
          }
        }
      }

      url = ub.toString();
    }

    return new URLHttpResponse(openConnection(url));
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param param
   *
   * @return
   */
  private String encode(String param)
  {
    try
    {
      param = URLEncoder.encode(param, ENCODING);
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }

    return param;
  }

  /**
   * Method description
   *
   *
   * @param spec
   *
   * @return
   *
   * @throws IOException
   */
  private URLConnection openConnection(String spec) throws IOException
  {
    return openConnection(new URL(spec));
  }

  /**
   * Method description
   *
   *
   * @param url
   *
   * @return
   *
   * @throws IOException
   */
  private URLConnection openConnection(URL url) throws IOException
  {
    URLConnection connection = null;

    if (configuration.isEnableProxy())
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("fetch '{}' using proxy {}:{}",
                     new Object[] { url.toExternalForm(),
                                    configuration.getProxyServer(),
                                    configuration.getProxyPort() });
      }

      SocketAddress address =
        new InetSocketAddress(configuration.getProxyServer(),
                              configuration.getProxyPort());

      connection = url.openConnection(new Proxy(Proxy.Type.HTTP, address));
    }
    else
    {
      if (logger.isDebugEnabled())
      {
        logger.debug("fetch '{}'", url.toExternalForm());
      }

      connection = url.openConnection();
    }

    connection.setRequestProperty(HEADER_ACCEPT_ENCODING,
                                  HEADER_ACCEPT_ENCODING_VALUE);
    connection.setRequestProperty(
        HEADER_USERAGENT, HEADER_USERAGENT_VALUE.concat(context.getVersion()));

    return connection;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private ScmConfiguration configuration;

  /** Field description */
  private SCMContextProvider context;
}
