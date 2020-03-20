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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

import sonia.scm.plugin.Extension;

/**
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 */
@Extension
public class BrowserUserAgentProvider implements UserAgentProvider
{

  /** Field description */
  @VisibleForTesting
  static final UserAgent CHROME = UserAgent.builder(
                                    "Chrome").basicAuthenticationCharset(
                                    Charsets.UTF_8).build();

  /** Field description */
  private static final String CHROME_PATTERN = "chrome";

  /** Field description */
  @VisibleForTesting
  static final UserAgent FIREFOX = UserAgent.builder("Firefox").build();

  /** Field description */
  private static final String FIREFOX_PATTERN = "firefox";

  /** Field description */
  @VisibleForTesting
  static final UserAgent MSIE = UserAgent.builder("Internet Explorer").build();

  /** Field description */
  private static final String MSIE_PATTERN = "msie";

  /** Field description */
  @VisibleForTesting    // todo check charset
  static final UserAgent SAFARI = UserAgent.builder("Safari").build();

  /** Field description */
  private static final String OPERA_PATTERN = "opera";

  /** Field description */
  private static final String SAFARI_PATTERN = "safari";

  /** Field description */
  @VisibleForTesting    // todo check charset
  static final UserAgent OPERA = UserAgent.builder(
                                   "Opera").basicAuthenticationCharset(
                                   Charsets.UTF_8).build();

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param userAgentString
   *
   * @return
   */
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
