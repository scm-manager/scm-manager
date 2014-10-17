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

import com.google.common.base.Charsets;
import com.google.common.collect.Sets;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.util.HttpUtil;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
 */
@RunWith(MockitoJUnitRunner.class)
public class UserAgentParserTest
{

  /** Field description */
  private static final String UA_1 = "mozilla/5.0";

  /** Field description */
  private static final String UA_2 = "wget/1.5.3";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void prepare()
  {
    Set<UserAgentProvider> providers = Sets.newHashSet(provider1, provider2);

    when(cacheManager.getCache(String.class, UserAgent.class,
      UserAgentParser.CACHE_NAME)).thenReturn(cache);
    parser = new UserAgentParser(providers, cacheManager);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testDefaultValues()
  {
    UserAgent ua = parser.parse(UA_1);

    assertEquals(Charsets.ISO_8859_1, ua.getBasicAuthenticationCharset());
    assertTrue(ua.isBrowser());
  }

  /**
   * Method description
   *
   */
  @Test
  public void testParse()
  {
    UserAgent ua = UserAgent.builder("UA1").build();

    when(provider1.parseUserAgent(UA_1)).thenReturn(ua);

    UserAgent ua2 = UserAgent.builder("UA2").build();

    when(provider2.parseUserAgent(UA_2)).thenReturn(ua2);

    assertEquals(ua, parser.parse(UA_1));
    assertEquals(ua2, parser.parse(UA_2));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testParseHttpServletRequest()
  {
    when(request.getHeader(HttpUtil.HEADER_USERAGENT)).thenReturn(UA_2);

    UserAgent ua = UserAgent.builder("UA2").build();

    when(provider1.parseUserAgent(UA_2)).thenReturn(ua);
    assertEquals(ua, parser.parse(request));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testParseNotFound()
  {
    assertEquals(UserAgentParser.UNKNOWN, parser.parse(UA_1));
    assertEquals(UserAgentParser.UNKNOWN, parser.parse(UA_2));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testParseWithCache()
  {
    UserAgent ua = UserAgent.builder("UA").build();

    when(cache.get(UA_1)).thenReturn(ua);
    assertEquals(ua, parser.parse(UA_1));
    assertEquals(UserAgentParser.UNKNOWN, parser.parse(UA_2));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Mock
  private Cache<String, UserAgent> cache;

  /** Field description */
  @Mock
  private CacheManager cacheManager;

  /** Field description */
  private UserAgentParser parser;

  /** Field description */
  @Mock
  private UserAgentProvider provider1;

  /** Field description */
  @Mock
  private UserAgentProvider provider2;

  /** Field description */
  @Mock
  private HttpServletRequest request;
}
