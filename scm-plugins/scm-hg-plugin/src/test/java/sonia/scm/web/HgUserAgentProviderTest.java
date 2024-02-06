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


import com.google.common.base.Strings;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Locale;

/**
 *
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
 */
public class HgUserAgentProviderTest
{

  private static final String UA_1 = "mercurial/proto-1.0";

  private static final String UA_2 =
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.124 Safari/537.36";


   @Test
  public void testParseUserAgent()
  {
    assertEquals(HgUserAgentProvider.HG, parse(UA_1));
    assertNull(parse(UA_2));
  }


  private UserAgent parse(String v)
  {
    return provider.parseUserAgent(
      Strings.nullToEmpty(v).toLowerCase(Locale.ENGLISH));
  }

  //~--- fields ---------------------------------------------------------------

  private final HgUserAgentProvider provider = new HgUserAgentProvider();
}
