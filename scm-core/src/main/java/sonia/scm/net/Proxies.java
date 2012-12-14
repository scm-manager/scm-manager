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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.GlobUtil;

//~--- JDK imports ------------------------------------------------------------

import java.net.URL;

/**
 * Util class for proxy settings.
 *
 * @author Sebastian Sdorra
 * @since 1.23
 */
public class Proxies
{

  /**
   * the logger for Proxies
   */
  private static final Logger logger = LoggerFactory.getLogger(Proxies.class);

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns true if proxy settings should be used to access the given url.
   *
   *
   * @param configuration scm-manager main configuration
   * @param url url to check
   *
   * @return true if proxy settings should be used
   */
  public static boolean isEnabled(ScmConfiguration configuration, String url)
  {
    boolean result = false;

    if (configuration.isEnableProxy())
    {
      result = true;

      int index = url.indexOf("://");

      if (index > 0)
      {
        url = url.substring(index + 3);
      }

      index = url.indexOf("/");

      if (index > 0)
      {
        url = url.substring(0, index);
      }

      for (String exclude : configuration.getProxyExcludes())
      {
        if (GlobUtil.matches(exclude, url))
        {
          logger.debug(
            "disable proxy settings for url {}, because exclude {} matches",
            url, exclude);
          result = false;

          break;
        }
      }
    }
    else
    {
      logger.trace("proxy settings are disabled");
    }

    return result;
  }

  /**
   * Returns true if proxy settings should be used to access the given url.
   *
   *
   * @param configuration scm-manager main configuration
   * @param url url to check
   *
   * @return true if proxy settings should be used
   */
  public static boolean isEnabled(ScmConfiguration configuration, URL url)
  {
    return isEnabled(configuration, url.getHost());
  }
}
