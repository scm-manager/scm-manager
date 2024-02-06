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


import org.junit.Test;


import static org.junit.Assert.*;

import java.util.Locale;

/**
 *
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
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
