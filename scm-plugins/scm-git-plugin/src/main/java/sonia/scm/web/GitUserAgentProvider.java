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
import com.google.common.base.Strings;
import java.util.Locale;

import sonia.scm.plugin.ext.Extension;

/**
 *
 * @author Sebastian Sdorra <sebastian.sdorra@gmail.com>
 * @since 1.45
 */
@Extension
public class GitUserAgentProvider implements UserAgentProvider
{
 
  private static final String PREFIX_JGIT = "jgit/";
  private static final String PREFIX_REGULAR = "git/";
  private static final String PREFIX_LFS = "git-lfs/";
  private static final String SUFFIX_MSYSGIT = "msysgit";

  @VisibleForTesting
  static final UserAgent JGIT = UserAgent.builder("JGit")
          .browser(false)
          .basicAuthenticationCharset(Charsets.UTF_8)
          .build();
  
  @VisibleForTesting
  static final UserAgent GIT = UserAgent.builder("Git")
          .browser(false)
          .basicAuthenticationCharset(Charsets.UTF_8)
          .build();

  @VisibleForTesting
  static final UserAgent GIT_LFS = UserAgent.builder("Git Lfs")
          .browser(false)
          .basicAuthenticationCharset(Charsets.UTF_8)
          .build();

  @VisibleForTesting
  static final UserAgent MSYSGIT = UserAgent.builder("msysGit")
          .browser(false)
          .basicAuthenticationCharset(Charsets.UTF_8)
          .build();



  //~--- methods --------------------------------------------------------------

  @Override
  public UserAgent parseUserAgent(String userAgentString)
  {
    String lowerUserAgent = toLower(userAgentString);
    
    if (isJGit(lowerUserAgent)) {
      return JGIT;
    } else if (isMsysGit(lowerUserAgent)) {
      return MSYSGIT;
    } else if (isGitLFS(lowerUserAgent)) {
      return GIT_LFS;
    } else if (isGit(lowerUserAgent)) {
      return GIT;
    } else {
      return null;
    }
  }
  
  private String toLower(String value) {
    return Strings.nullToEmpty(value).toLowerCase(Locale.ENGLISH);
  }
  
  private boolean isJGit(String userAgent) {
    return userAgent.startsWith(PREFIX_JGIT);
  }
  
  private boolean isMsysGit(String userAgent) {
    return userAgent.startsWith(PREFIX_REGULAR) && userAgent.contains(SUFFIX_MSYSGIT);
  }
  
  private boolean isGitLFS(String userAgent) {
    return userAgent.startsWith(PREFIX_LFS);
  }
  
  private boolean isGit(String userAgent) {
    return userAgent.startsWith(PREFIX_REGULAR);
  }
}
