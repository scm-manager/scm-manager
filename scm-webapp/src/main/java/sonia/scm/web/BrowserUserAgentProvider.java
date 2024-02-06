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
