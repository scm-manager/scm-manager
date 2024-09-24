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

import sonia.scm.plugin.Extension;

import java.nio.charset.Charset;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.45
 */
@Extension
public class HgUserAgentProvider implements UserAgentProvider
{

  /** mercurial seems to use system encoding */
  @VisibleForTesting
  static UserAgent HG = UserAgent.scmClient("Mercurial").basicAuthenticationCharset(
                          Charset.defaultCharset()).build();

  private static final String PREFIX = "mercurial";



  @Override
  public UserAgent parseUserAgent(String userAgentString)
  {
    UserAgent ua = null;

    if (userAgentString.startsWith(PREFIX))
    {
      ua = HG;
    }

    return ua;
  }
}
