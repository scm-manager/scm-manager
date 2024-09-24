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
