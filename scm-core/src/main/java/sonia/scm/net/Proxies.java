/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
