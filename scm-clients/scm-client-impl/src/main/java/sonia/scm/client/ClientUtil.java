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



package sonia.scm.client;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;

/**
 *
 * @author Sebastian Sdorra
 */
public class ClientUtil
{

  /** the logger for ClientUtil */
  private static final Logger logger =
    LoggerFactory.getLogger(ClientUtil.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param exception
   * @param response
   */
  public static void appendContent(ScmClientException exception,
                                   ClientResponse response)
  {
    try
    {
      exception.setContent(response.getEntity(String.class));
    }
    catch (Exception ex)
    {
      logger.warn("could not read content", ex);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param expectedStatusCode
   */
  public static void checkResponse(ClientResponse response,
                                   int expectedStatusCode)
  {
    int sc = response.getStatus();

    if (sc != expectedStatusCode)
    {
      sendException(response, sc);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   *
   */
  public static void checkResponse(ClientResponse response)
  {
    int sc = response.getStatus();

    if (sc >= 300)
    {
      sendException(response, sc);
    }
  }

  /**
   * Method description
   *
   *
   * @param response
   */
  public static void close(ClientResponse response)
  {
    if (response == null)
    {
      response.close();
    }
  }

  /**
   * Method description
   *
   *
   * @param client
   * @param url
   *
   * @return
   */
  public static WebResource createResource(Client client, String url)
  {
    return createResource(client, url, false);
  }

  /**
   * Method description
   *
   *
   * @param client
   * @param url
   * @param enableLogging
   *
   * @return
   */
  public static WebResource createResource(Client client, String url,
          boolean enableLogging)
  {
    WebResource resource = client.resource(url);

    if (enableLogging)
    {
      resource.addFilter(new LoggingFilter());
    }

    return resource;
  }

  /**
   * Method description
   *
   *
   * @param response
   * @param sc
   */
  public static void sendException(ClientResponse response, int sc)
  {
    ScmClientException exception = null;

    switch (sc)
    {
      case ScmClientException.SC_UNAUTHORIZED :
        exception = new ScmUnauthorizedException();

        break;

      case ScmClientException.SC_FORBIDDEN :
        exception = new ScmForbiddenException();

        break;

      case ScmClientException.SC_NOTFOUND :
        exception = new ScmNotFoundException();

        break;

      default :
        exception = new ScmClientException(sc);
        appendContent(exception, response);
    }

    throw exception;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param response
   *
   * @return
   */
  public static boolean isSuccessfull(ClientResponse response)
  {
    int status = response.getStatus();

    return (status > 200) && (status < 300);
  }
}
