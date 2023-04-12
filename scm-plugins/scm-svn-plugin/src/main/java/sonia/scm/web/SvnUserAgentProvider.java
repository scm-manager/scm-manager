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
 * @since 1.45
 */
@Extension
public final class SvnUserAgentProvider implements UserAgentProvider
{

  /** ua prefix */
  private static final String PREFIX = "svn";

  /** ua suffix */
  private static final String SUFFIX = "tortoisesvn";

  /** TortoiseSVN */
  @VisibleForTesting
  static final UserAgent TORTOISE_SVN = 
    UserAgent.scmClient("TortoiseSVN")
             .basicAuthenticationCharset(Charsets.UTF_8).build();

  /** Subversion cli client */
  @VisibleForTesting
  static final UserAgent SVN =
    UserAgent.scmClient("Subversion")
             .basicAuthenticationCharset(Charsets.UTF_8).build();

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

    if (userAgentString.startsWith(PREFIX))
    {
      if (userAgentString.contains(SUFFIX))
      {
        ua = TORTOISE_SVN;
      }
      else
      {
        ua = SVN;
      }
    }

    return ua;
  }
}
