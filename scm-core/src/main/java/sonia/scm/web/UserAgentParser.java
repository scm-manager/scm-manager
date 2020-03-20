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
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.util.HttpUtil;

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Parser for User-Agent header. The UserAgentParser parses the User-Agent 
 * header and returns a {@link UserAgent} object.
 *
 * @author Sebastian Sdorra <s.sdorra@gmail.com>
 * @since 1.45
 */
@Singleton
public final class UserAgentParser
{

  /** name of the cache */
  @VisibleForTesting
  static final String CACHE_NAME = "sonia.scm.user-agent";

  /** unknown UserAgent */
  @VisibleForTesting
  static final UserAgent UNKNOWN = UserAgent.builder("UNKNOWN").build();

  /** logger */
  private static final Logger logger =
    LoggerFactory.getLogger(UserAgentParser.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new UserAgentParser.
   *
   *
   * @param providers set of providers
   * @param cacheManager cache manager
   */
  @Inject
  public UserAgentParser(Set<UserAgentProvider> providers,
    CacheManager cacheManager)
  {
    this.providers = providers;
    this.cache = cacheManager.getCache(CACHE_NAME);
  }

  //~--- methods --------------------------------------------------------------

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
   *
   * @return {@link UserAgent} object
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

  //~--- fields ---------------------------------------------------------------

  /** cache for parsed UserAgents */
  private final Cache<String, UserAgent> cache;

  /** set of providers */
  private final Set<UserAgentProvider> providers;
}
