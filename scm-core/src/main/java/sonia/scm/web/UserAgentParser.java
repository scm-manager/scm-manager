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
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.util.HttpUtil;

import java.util.Locale;
import java.util.Set;

/**
 * Parser for User-Agent header. The UserAgentParser parses the User-Agent 
 * header and returns a {@link UserAgent} object.
 *
 * @since 1.45
 */
@Singleton
public final class UserAgentParser
{

  @VisibleForTesting
  static final String CACHE_NAME = "sonia.scm.user-agent";

  /** unknown UserAgent */
  @VisibleForTesting
  static final UserAgent UNKNOWN = UserAgent.other("UNKNOWN").build();

  private static final Logger logger =
    LoggerFactory.getLogger(UserAgentParser.class);

  /** cache for parsed UserAgents */
  private final Cache<String, UserAgent> cache;

  /** set of providers */
  private final Set<UserAgentProvider> providers;

  @Inject
  public UserAgentParser(Set<UserAgentProvider> providers,
    CacheManager cacheManager)
  {
    this.providers = providers;
    this.cache = cacheManager.getCache(CACHE_NAME);
  }


  /**
   * Extracts the User-Agent header and returns an {@link UserAgent} object.
   *
   *
   * @param request http request
   *
   * @return {@link UserAgent} object
   */
  public UserAgent parse(HttpServletRequest request)
  {
    return parse(request.getHeader(HttpUtil.HEADER_USERAGENT));
  }

  /**
   * Parses the User-Agent header and returns a {@link UserAgent} object.
   *
   *
   * @param userAgent User-Agent header
   */
  public UserAgent parse(String userAgent)
  {
    String uas = Strings.nullToEmpty(userAgent).toLowerCase(Locale.ENGLISH);
    UserAgent ua = cache.get(uas);

    if (ua == null)
    {
      for (UserAgentProvider provider : providers)
      {
        ua = provider.parseUserAgent(uas);

        if (ua != null)
        {
          break;
        }
      }

      if (ua == null)
      {
        ua = UNKNOWN;
      }

      cache.put(uas, ua);
    }

    logger.trace("return user-agent {} for {}", ua, userAgent);

    return ua;
  }

}
