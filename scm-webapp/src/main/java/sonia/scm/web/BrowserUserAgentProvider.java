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

package sonia.scm.web;


import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

import sonia.scm.plugin.Extension;

@Extension
public class BrowserUserAgentProvider implements UserAgentProvider
{

  @VisibleForTesting
  static final UserAgent CHROME = UserAgent.browser(
                                    "Chrome").basicAuthenticationCharset(
                                    Charsets.UTF_8).build();

  private static final String CHROME_PATTERN = "chrome";

  @VisibleForTesting
  static final UserAgent FIREFOX = UserAgent.browser("Firefox").build();

  private static final String FIREFOX_PATTERN = "firefox";

  @VisibleForTesting
  static final UserAgent MSIE = UserAgent.browser("Internet Explorer").build();

  private static final String MSIE_PATTERN = "msie";

  @VisibleForTesting    // todo check charset
  static final UserAgent SAFARI = UserAgent.browser("Safari").build();

  private static final String OPERA_PATTERN = "opera";

  private static final String SAFARI_PATTERN = "safari";

  @VisibleForTesting    // todo check charset
  static final UserAgent OPERA = UserAgent.browser(
                                   "Opera").basicAuthenticationCharset(
                                   Charsets.UTF_8).build();



  @Override
  public UserAgent parseUserAgent(String userAgentString)
  {
    UserAgent ua = null;

    if (userAgentString.contains(CHROME_PATTERN))
    {
      ua = CHROME;
    }
    else if (userAgentString.contains(FIREFOX_PATTERN))
    {
      ua = FIREFOX;
    }
    else if (userAgentString.contains(OPERA_PATTERN))
    {
      ua = OPERA;
    }
    else if (userAgentString.contains(MSIE_PATTERN))
    {
      ua = MSIE;
    }
    else if (userAgentString.contains(SAFARI_PATTERN))
    {
      ua = SAFARI;
    }

    return ua;
  }
}
