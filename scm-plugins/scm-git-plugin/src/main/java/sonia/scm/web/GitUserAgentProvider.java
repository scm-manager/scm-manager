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
import com.google.common.base.Strings;
import java.util.Locale;

import sonia.scm.plugin.Extension;

/**
 * UserAgent provider for git related clients.
 * @since 1.45
 */
@Extension
public class GitUserAgentProvider implements UserAgentProvider {

  private static final String PREFIX_JGIT = "jgit/";

  @VisibleForTesting
  static final UserAgent JGIT = UserAgent.scmClient("JGit")
          .basicAuthenticationCharset(Charsets.UTF_8)
          .build();

  private static final String PREFIX_REGULAR = "git/";

  @VisibleForTesting
  static final UserAgent GIT = UserAgent.scmClient("Git")
          .basicAuthenticationCharset(Charsets.UTF_8)
          .build();

  private static final String PREFIX_LFS = "git-lfs/";

  @VisibleForTesting
  static final UserAgent GIT_LFS = UserAgent.scmClient("Git Lfs")
          .basicAuthenticationCharset(Charsets.UTF_8)
          .build();

  private static final String SUFFIX_MSYSGIT = "msysgit";

  @VisibleForTesting
  static final UserAgent MSYSGIT = UserAgent.scmClient("msysGit")
          .basicAuthenticationCharset(Charsets.UTF_8)
          .build();




  @Override
  public UserAgent parseUserAgent(String userAgentString) {
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
