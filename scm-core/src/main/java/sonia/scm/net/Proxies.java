/*
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
    
package sonia.scm.net;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.GlobUtil;

import java.net.URL;

/**
 * Util class for proxy settings.
 *
 * @since 1.23
 */
public final class Proxies
{

  private static final Logger logger = LoggerFactory.getLogger(Proxies.class);


  private Proxies() {}


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

      index = url.indexOf('/');

      if (index > 0)
      {
        url = url.substring(0, index);
      }

      index = url.indexOf(':');

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
