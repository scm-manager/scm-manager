/**
* Copyright (c) 2010, Sebastian Sdorra
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* 1. Redistributions of source code must retain the above copyright notice,
*    this list of conditions and the following disclaimer.
* 2. Redistributions in binary form must reproduce the above copyright notice,
*    this list of conditions and the following disclaimer in the documentation
*    and/or other materials provided with the distribution.
* 3. Neither the name of SCM-Manager; nor the names of its
*    contributors may be used to endorse or promote products derived from this
*    software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
* ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*
* http://bitbucket.org/sdorra/scm-manager
*
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
