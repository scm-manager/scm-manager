/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

import sonia.scm.plugin.ext.Extension;

/**
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
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
    UserAgent.builder("TortoiseSVN").browser(false)
             .basicAuthenticationCharset(Charsets.UTF_8).build();

  /** Subversion cli client */
  @VisibleForTesting
  static final UserAgent SVN =
    UserAgent.builder("Subversion").browser(false)
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
