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


import com.google.common.base.Charsets;
import com.google.common.collect.Sets;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.cache.Cache;
import sonia.scm.cache.CacheManager;
import sonia.scm.util.HttpUtil;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

/**
 *
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
 */
@SuppressWarnings("unchecked")
@RunWith(MockitoJUnitRunner.class)
public class UserAgentParserTest
{

  private static final String UA_1 = "mozilla/5.0";

  private static final String UA_2 = "wget/1.5.3";


   @Before
  public void prepare()
  {
    Set<UserAgentProvider> providers = Sets.newHashSet(provider1, provider2);
    when(cacheManager.getCache(UserAgentParser.CACHE_NAME)).thenReturn(cache);
    parser = new UserAgentParser(providers, cacheManager);
  }

   @Test
  public void testDefaultValues()
  {
    UserAgent ua = parser.parse(UA_1);

    assertEquals(Charsets.UTF_8, ua.getBasicAuthenticationCharset());
    assertFalse(ua.isBrowser());
  }

   @Test
  public void testParse()
  {
    UserAgent ua = UserAgent.other("UA1").build();

    when(provider1.parseUserAgent(UA_1)).thenReturn(ua);

    UserAgent ua2 = UserAgent.other("UA2").build();

    when(provider2.parseUserAgent(UA_2)).thenReturn(ua2);

    assertEquals(ua, parser.parse(UA_1));
    assertEquals(ua2, parser.parse(UA_2));
  }

   @Test
  public void testParseHttpServletRequest()
  {
    when(request.getHeader(HttpUtil.HEADER_USERAGENT)).thenReturn(UA_2);

    UserAgent ua = UserAgent.other("UA2").build();

    when(provider1.parseUserAgent(UA_2)).thenReturn(ua);
    assertEquals(ua, parser.parse(request));
  }

   @Test
  public void testParseNotFound()
  {
    assertEquals(UserAgentParser.UNKNOWN, parser.parse(UA_1));
    assertEquals(UserAgentParser.UNKNOWN, parser.parse(UA_2));
  }

   @Test
  public void testParseWithCache()
  {
    UserAgent ua = UserAgent.other("UA").build();

    when(cache.get(UA_1)).thenReturn(ua);
    assertEquals(ua, parser.parse(UA_1));
    assertEquals(UserAgentParser.UNKNOWN, parser.parse(UA_2));
  }

  //~--- fields ---------------------------------------------------------------

  @Mock
  private Cache cache;

  @Mock
  private CacheManager cacheManager;

  private UserAgentParser parser;

  @Mock
  private UserAgentProvider provider1;

  @Mock
  private UserAgentProvider provider2;

  @Mock
  private HttpServletRequest request;
}
