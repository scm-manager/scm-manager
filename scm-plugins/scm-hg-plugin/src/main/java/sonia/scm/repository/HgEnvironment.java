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



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.ProvisionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.web.HgUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public final class HgEnvironment
{

  private static final Logger LOG = LoggerFactory.getLogger(HgEnvironment.class);

  /** Field description */
  public static final String ENV_PYTHON_PATH = "PYTHONPATH";

  /** Field description */
  private static final String ENV_CHALLENGE = "SCM_CHALLENGE";

  /** Field description */
  private static final String ENV_URL = "SCM_URL";

  private static final String SCM_BEARER_TOKEN = "SCM_BEARER_TOKEN";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private HgEnvironment() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param environment
   * @param handler
   * @param hookManager
   */
  public static void prepareEnvironment(Map<String, String> environment,
                                        HgRepositoryHandler handler, HgHookManager hookManager)
  {
    prepareEnvironment(environment, handler, hookManager, null);
  }

  /**
   * Method description
   *
   *
   * @param environment
   * @param handler
   * @param hookManager
   * @param request
   */
  public static void prepareEnvironment(Map<String, String> environment,
    HgRepositoryHandler handler, HgHookManager hookManager,
    HttpServletRequest request)
  {
    String hookUrl;

    if (request != null)
    {
      hookUrl = hookManager.createUrl(request);
    }
    else
    {
      hookUrl = hookManager.createUrl();
    }

    try {
      String credentials = hookManager.getCredentials();
      environment.put(SCM_BEARER_TOKEN, credentials);
    } catch (ProvisionException e) {
      LOG.debug("could not create bearer token; looks like currently we are not in a request; probably you can ignore the following exception:", e);
    }
    environment.put(ENV_PYTHON_PATH, HgUtil.getPythonPath(handler.getConfig()));
    environment.put(ENV_URL, hookUrl);
    environment.put(ENV_CHALLENGE, hookManager.getChallenge());
  }
}
