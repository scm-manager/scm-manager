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
