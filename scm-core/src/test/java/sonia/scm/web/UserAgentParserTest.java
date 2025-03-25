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
 * @author Sebastian Sdorra
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
