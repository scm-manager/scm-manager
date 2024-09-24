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


import com.google.common.base.Strings;

import org.junit.Test;


import static org.junit.Assert.*;


import java.util.Locale;

/**
 *
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
 */
public class BrowserUserAgentProviderTest
{

  private static final String CHROME =
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.124 Safari/537.36";

  private static final String FIREFOX =
    "Mozilla/5.0 (Windows; U; Windows NT 5.2; en-GB; rv:1.8.1.18) Gecko/20081029 Firefox/2.0.0.18";

  private static final String MSIE =
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; )";

  private static final String OPERA =
    "Opera/9.80 (Windows NT 5.1; U; cs) Presto/2.2.15 Version/10.00";

  private static final String SAFARI =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/6.1.3 Safari/537.75.14";

  private static final String WGET = "Wget/1.5.3";


   @Test
  public void testParseUserAgent()
  {
    assertEquals(BrowserUserAgentProvider.MSIE, parse(MSIE));
    assertEquals(BrowserUserAgentProvider.FIREFOX, parse(FIREFOX));
    assertEquals(BrowserUserAgentProvider.OPERA, parse(OPERA));
    assertEquals(BrowserUserAgentProvider.CHROME, parse(CHROME));
    assertEquals(BrowserUserAgentProvider.SAFARI, parse(SAFARI));
    assertNull(parse(WGET));
  }


  private UserAgent parse(String v)
  {
    return provider.parseUserAgent(
      Strings.nullToEmpty(v).toLowerCase(Locale.ENGLISH));
  }

  //~--- fields ---------------------------------------------------------------

  private final BrowserUserAgentProvider provider =
    new BrowserUserAgentProvider();
}
