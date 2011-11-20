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

import org.sonatype.spice.jersey.client.ahc.AhcHttpClient;
import org.sonatype.spice.jersey.client.ahc.config.DefaultAhcConfig;

import sonia.scm.ScmState;
import sonia.scm.url.UrlProvider;
import sonia.scm.url.UrlProviderFactory;
import sonia.scm.util.AssertUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MultivaluedMap;

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

    if (logger.isInfoEnabled())
    {
      logger.info("create new session for {} with username {}", url, user);
    }

    UrlProvider urlProvider = UrlProviderFactory.createUrlProvider(url,
                                UrlProviderFactory.TYPE_RESTAPI_XML);
    DefaultAhcConfig config = new DefaultAhcConfig();
    AhcHttpClient client = AhcHttpClient.create(config);
    ClientResponse response = null;

    if (Util.isNotEmpty(username) && Util.isNotEmpty(password))
    {
      WebResource resource = ClientUtil.createResource(client,
                               urlProvider.getAuthenticationUrl(),
                               enableLogging);

      if (logger.isDebugEnabled())
      {
        logger.debug("try login for {}", username);
      }

      MultivaluedMap<String, String> formData = new MultivaluedMapImpl();

      formData.add("username", username);
      formData.add("password", password);
      response = resource.type("application/x-www-form-urlencoded").post(
        ClientResponse.class, formData);
    }
    else
    {
      WebResource resource = ClientUtil.createResource(client,
                               urlProvider.getStateUrl(), enableLogging);

      if (logger.isDebugEnabled())
      {
        logger.debug("try anonymous login");
      }

      response = resource.get(ClientResponse.class);
    }

    ClientUtil.checkResponse(response);

    ScmState state = response.getEntity(ScmState.class);

    if (!state.isSuccess())
    {
      if (logger.isWarnEnabled())
      {
        logger.warn("server returned state failed");
      }

      throw new ScmClientException("create ScmClientSession failed");
    }
    else if (logger.isInfoEnabled())
    {
      logger.info("create session successfully for user {}", user);
    }

    return new JerseyClientSession(client, urlProvider, state);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean enableLogging = false;
}
