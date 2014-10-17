/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Strings;

import org.junit.Test;


import static org.junit.Assert.*;


//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;

/**
 *
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
 */
public class BrowserUserAgentProviderTest
{

  /** Field description */
  private static final String CHROME =
    "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2062.124 Safari/537.36";

  /** Field description */
  private static final String FIREFOX =
    "Mozilla/5.0 (Windows; U; Windows NT 5.2; en-GB; rv:1.8.1.18) Gecko/20081029 Firefox/2.0.0.18";

  /** Field description */
  private static final String MSIE =
    "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; )";

  /** Field description */
  private static final String OPERA =
    "Opera/9.80 (Windows NT 5.1; U; cs) Presto/2.2.15 Version/10.00";

  /** Field description */
  private static final String SAFARI =
    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/6.1.3 Safari/537.75.14";

  /** Field description */
  private static final String WGET = "Wget/1.5.3";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
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

  /**
   * Method description
   *
   *
   * @param v
   *
   * @return
   */
  private UserAgent parse(String v)
  {
    return provider.parseUserAgent(
      Strings.nullToEmpty(v).toLowerCase(Locale.ENGLISH));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final BrowserUserAgentProvider provider =
    new BrowserUserAgentProvider();
}
