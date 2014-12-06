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

import sonia.scm.ScmState;
import sonia.scm.url.UrlProvider;
import sonia.scm.url.UrlProviderFactory;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.sun.jersey.multipart.impl.MultiPartWriter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.NewCookie;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyClientProvider implements ScmClientProvider
{

  /** the logger for JerseyClientProvider */
  private static final Logger logger =
    LoggerFactory.getLogger(JerseyClientProvider.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public JerseyClientProvider() {}

  /**
   * Constructs ...
   *
   *
   * @param enableLogging
   */
  public JerseyClientProvider(boolean enableLogging)
  {
    this.enableLogging = enableLogging;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param url
   * @param username
   * @param password
   *
   * @return
   *
   */
  @Override
  public JerseyClientSession createSession(String url, String username,
    String password)
  {
    AssertUtil.assertIsNotEmpty(url);

    String user = "anonymous";

    if (Util.isNotEmpty(username))
    {
      user = username;
    }

    logger.info("create new session for {} with username {}", url, user);

    UrlProvider urlProvider = UrlProviderFactory.createUrlProvider(url,
                                UrlProviderFactory.TYPE_RESTAPI_XML);

    Client client =
      Client.create(new DefaultClientConfig(MultiPartWriter.class));

    client.addFilter(new CookieClientFilter());

    ClientResponse response;

    if (Util.isNotEmpty(username) && Util.isNotEmpty(password))
    {
      response = login(urlProvider, client, username, password);
    }
    else
    {
      response = state(urlProvider, client);
    }

    ClientUtil.checkResponse(response);

    ScmState state = response.getEntity(ScmState.class);

    if (!state.isSuccess())
    {
      logger.warn("server returned state failed");

      throw new ScmClientException("create ScmClientSession failed");
    }
    else if (logger.isInfoEnabled())
    {
      logger.info("create session successfully for user {}", user);
    }

    return new JerseyClientSession(client, urlProvider, state);
  }

  /**
   * Method description
   *
   *
   * @param urlProvider
   * @param client
   * @param username
   * @param password
   *
   * @return
   */
  private ClientResponse login(UrlProvider urlProvider, Client client,
    String username, String password)
  {
    String authUrl = urlProvider.getAuthenticationUrl();

    if (logger.isDebugEnabled())
    {
      logger.debug("try login at {}", authUrl);
    }

    WebResource resource = ClientUtil.createResource(client, authUrl,
                             enableLogging);

    if (logger.isDebugEnabled())
    {
      logger.debug("try login for {}", username);
    }

    MultivaluedMap<String, String> formData = new MultivaluedMapImpl();

    formData.add("username", username);
    formData.add("password", password);

    return resource.type("application/x-www-form-urlencoded").post(
      ClientResponse.class, formData);
  }

  /**
   * Method description
   *
   *
   * @param urlProvider
   * @param client
   *
   * @return
   */
  private ClientResponse state(UrlProvider urlProvider, Client client)
  {
    String stateUrl = urlProvider.getStateUrl();

    if (logger.isDebugEnabled())
    {
      logger.debug("retrive state from {}", stateUrl);
    }

    WebResource resource = ClientUtil.createResource(client, stateUrl,
                             enableLogging);

    if (logger.isDebugEnabled())
    {
      logger.debug("try anonymous login");
    }

    return resource.get(ClientResponse.class);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/07/05
   * @author         Enter your name here...
   */
  private class CookieClientFilter extends ClientFilter
  {

    /**
     * Method description
     *
     *
     * @param request
     *
     * @return
     *
     * @throws ClientHandlerException
     */
    @Override
    public ClientResponse handle(ClientRequest request)
      throws ClientHandlerException
    {
      for (NewCookie c : cookies)
      {
        request.getHeaders().putSingle("Cookie", c);
      }

      ClientResponse response = getNext().handle(request);

      if (response.getCookies() != null)
      {
        cookies.addAll(response.getCookies());
      }

      return response;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final List<NewCookie> cookies =
      Collections.synchronizedList(new ArrayList<NewCookie>());
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean enableLogging = false;
}
