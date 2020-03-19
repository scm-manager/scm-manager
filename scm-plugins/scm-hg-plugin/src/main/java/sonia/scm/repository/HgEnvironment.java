/**
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.ProvisionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.AccessToken;
import sonia.scm.security.CipherUtil;
import sonia.scm.security.Xsrf;
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

  private static final String SCM_XSRF = "SCM_XSRF";

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
      AccessToken accessToken = hookManager.getAccessToken();
      environment.put(SCM_BEARER_TOKEN, CipherUtil.getInstance().encode(accessToken.compact()));
      extractXsrfKey(environment, accessToken);
    } catch (ProvisionException e) {
      LOG.debug("could not create bearer token; looks like currently we are not in a request; probably you can ignore the following exception:", e);
    }
    environment.put(ENV_PYTHON_PATH, HgUtil.getPythonPath(handler.getConfig()));
    environment.put(ENV_URL, hookUrl);
    environment.put(ENV_CHALLENGE, hookManager.getChallenge());
  }

  private static void extractXsrfKey(Map<String, String> environment, AccessToken accessToken) {
    environment.put(SCM_XSRF, accessToken.<String>getCustom(Xsrf.TOKEN_KEY).orElse("-"));
  }
}
