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



package sonia.scm.client.it;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import sonia.scm.client.ClientUtil;
import sonia.scm.client.JerseyClientProvider;
import sonia.scm.client.JerseyClientSession;
import sonia.scm.client.ScmClientException;
import sonia.scm.client.ScmUrlProvider;
import sonia.scm.config.ScmConfiguration;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 *
 * @author Sebastian Sdorra
 */
public class JerseyClientProviderITCase
{

  /**
   * Method description
   *
   *
   * @throws ScmClientException
   */
  @Test(expected = ScmClientException.class)
  public void createSessionAnonymousFailedTest() throws ScmClientException
  {
    createSession(null, null);
  }

  /**
   * Method description
   *
   *
   * @throws ScmClientException
   */
  @Test
  public void createSessionAnonymousTest() throws ScmClientException
  {
    JerseyClientSession adminSession = createSession("scmadmin", "scmadmin");

    // enable anonymous access
    ScmUrlProvider up = adminSession.getUrlProvider();
    Client client = adminSession.getClient();
    WebResource resource = ClientUtil.createResource(client,
                             up.getResourceUrl("config"), true);
    ScmConfiguration config = resource.get(ScmConfiguration.class);

    config.setAnonymousAccessEnabled(true);
    resource.post(config);

    // test anonymous access
    createSession(null, null);

    // disable anonymous access
    config.setAnonymousAccessEnabled(false);
    resource.post(config);
  }

  /**
   * Method description
   *
   *
   * @throws ScmClientException
   */
  @Test
  public void createSessionTest() throws ScmClientException
  {
    JerseyClientSession session = createSession("scmadmin", "scmadmin");

    assertNotNull(session);
    assertNotNull(session.getState());
    assertNotNull(session.getState().getUser());
    assertEquals(session.getState().getUser().getName(), "scmadmin");
  }

  /**
   * Method description
   *
   *
   * @throws ScmClientException
   */
  @Test(expected = ScmClientException.class)
  public void createSessionWithUnkownUserTest() throws ScmClientException
  {
    createSession("dent", "dent123");
  }

  /**
   * Method description
   *
   *
   * @throws ScmClientException
   */
  @Test(expected = ScmClientException.class)
  public void createSessionWithWrongPasswordTest() throws ScmClientException
  {
    createSession("scmadmin", "ka123");
  }

  /**
   * Method description
   *
   *
   * @param username
   * @param password
   *
   * @return
   *
   * @throws ScmClientException
   */
  private JerseyClientSession createSession(String username, String password)
          throws ScmClientException
  {
    JerseyClientProvider provider = new JerseyClientProvider(true);

    return provider.createSession("http://localhost:8081/scm", username,
                                  password);
  }
}
