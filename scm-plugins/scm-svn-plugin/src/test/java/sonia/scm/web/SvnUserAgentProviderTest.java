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

import org.junit.Test;


import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.util.Locale;

/**
 *
 * @author Sebastian Sdorra <sebastian.sdorra@triology.de>
 */
public class SvnUserAgentProviderTest
{

  /** Field description */
  private static final String UA_1 =
    "SVN/1.8.8 (x64-microsoft-windows) serf/1.3.4 TortoiseSVN-1.8.6.25419";

  /** Field description */
  private static final String UA_2 = "SVN/1.5.4 (r33841) neon/0.28.3";

  /** Field description */
  private static final String UA_3 = "SVN/1.6.3 (r38063) neon/0.28.4";

  /** Field description */
  private static final String UA_4 =
    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0;Google Wireless Transcoder;)";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  @Test
  public void testParseUserAgent()
  {
    assertEquals(SvnUserAgentProvider.TORTOISE_SVN, parse(UA_1));
    assertEquals(SvnUserAgentProvider.SVN, parse(UA_2));
    assertEquals(SvnUserAgentProvider.SVN, parse(UA_3));
    assertNull(parse(UA_4));
  }

  /**
   * Method description
   *
   *
   * @param ua
   *
   * @return
   */
  private UserAgent parse(String ua)
  {
    return suap.parseUserAgent(ua.toLowerCase(Locale.ENGLISH));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final SvnUserAgentProvider suap = new SvnUserAgentProvider();
}
