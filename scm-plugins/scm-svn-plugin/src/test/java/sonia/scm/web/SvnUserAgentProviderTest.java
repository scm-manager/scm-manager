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


import org.junit.Test;


import static org.junit.Assert.*;

import java.util.Locale;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnUserAgentProviderTest
{

  private static final String UA_1 =
    "SVN/1.8.8 (x64-microsoft-windows) serf/1.3.4 TortoiseSVN-1.8.6.25419";

  private static final String UA_2 = "SVN/1.5.4 (r33841) neon/0.28.3";

  private static final String UA_3 = "SVN/1.6.3 (r38063) neon/0.28.4";

  private static final String UA_4 =
    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0;Google Wireless Transcoder;)";


   @Test
  public void testParseUserAgent()
  {
    assertEquals(SvnUserAgentProvider.TORTOISE_SVN, parse(UA_1));
    assertEquals(SvnUserAgentProvider.SVN, parse(UA_2));
    assertEquals(SvnUserAgentProvider.SVN, parse(UA_3));
    assertNull(parse(UA_4));
  }


  private UserAgent parse(String ua)
  {
    return suap.parseUserAgent(ua.toLowerCase(Locale.ENGLISH));
  }

  //~--- fields ---------------------------------------------------------------

  private final SvnUserAgentProvider suap = new SvnUserAgentProvider();
}
