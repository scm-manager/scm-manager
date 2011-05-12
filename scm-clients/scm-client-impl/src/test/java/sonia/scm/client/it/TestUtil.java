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

import sonia.scm.client.ClientUtil;
import sonia.scm.client.JerseyClientProvider;
import sonia.scm.client.JerseyClientSession;
import sonia.scm.client.ScmUrlProvider;
import sonia.scm.config.ScmConfiguration;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

/**
 *
 * @author Sebastian Sdorra
 */
public class TestUtil
{

  /** Field description */
  public static final String ADMIN_PASSWORD = "scmadmin";

  /** Field description */
  public static final String ADMIN_USERNAME = "scmadmin";

  /** Field description */
  public static final String REPOSITORY_TYPE = "git";

  /** Field description */
  public static final String URL_BASE = "http://localhost:8081/scm";

  /** Field description */
  public static final boolean REQUEST_LOGGING = false;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   */
  public static JerseyClientSession createAdminSession()
  {
    return createSession(ADMIN_USERNAME, ADMIN_PASSWORD);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   */
  public static JerseyClientSession createAnonymousSession()
  {
    return createSession(null, null);
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
   */
  public static JerseyClientSession createSession(String username,
          String password)
  {
    JerseyClientProvider provider = new JerseyClientProvider(REQUEST_LOGGING);

    return provider.createSession(URL_BASE, username, password);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param access
   *
   */
  public static void setAnonymousAccess(boolean access)
  {
    JerseyClientSession adminSession = createAdminSession();
    ScmUrlProvider up = adminSession.getUrlProvider();
    Client client = adminSession.getClient();
    WebResource resource = ClientUtil.createResource(client,
                             up.getResourceUrl("config"), REQUEST_LOGGING);
    ScmConfiguration config = resource.get(ScmConfiguration.class);

    config.setAnonymousAccessEnabled(access);
    resource.post(config);
    adminSession.close();
  }
}
