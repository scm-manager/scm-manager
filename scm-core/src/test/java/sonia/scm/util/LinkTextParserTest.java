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



package sonia.scm.util;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

//~--- JDK imports ------------------------------------------------------------

import java.text.MessageFormat;

/**
 *
 * @author Sebastian Sdorra
 */
public class LinkTextParserTest
{

  /**
   * Method description
   *
   */
  @Test
  public void complexUrlParseTextTest()
  {
    assertEquals(createLink("http://www.scm-manager.org/test/"),
                 LinkTextParser.parseText("http://www.scm-manager.org/test/"));
    assertEquals(
        createLink("http://www.scm-manager.org/test/index.html"),
        LinkTextParser.parseText("http://www.scm-manager.org/test/index.html"));
    assertEquals(
        createLink("http://www.scm-manager.org/test/index.html?test=123"),
        LinkTextParser.parseText(
          "http://www.scm-manager.org/test/index.html?test=123"));
    assertEquals(
        createLink(
          "http://www.scm-manager.org/test/index.html?test=123&otherTest=123"), LinkTextParser
            .parseText(
              "http://www.scm-manager.org/test/index.html?test=123&otherTest=123"));
    assertEquals(
        createLink("http://www.scm-manager.org/test/index.html#hashparam"),
        LinkTextParser.parseText(
          "http://www.scm-manager.org/test/index.html#hashparam"));
    assertEquals(
        createLink(
          "http://www.scm-manager.org/test/index.html#hashparam;otherHash"), LinkTextParser
            .parseText(
              "http://www.scm-manager.org/test/index.html#hashparam;otherHash"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void simpleParseTextTest()
  {
    assertEquals(
        createLink("http://www.scm-manager.org", "http://www.scm-manager.org"),
        LinkTextParser.parseText("http://www.scm-manager.org"));
    assertEquals(
        createLink("http://www.scm-manager.org", "www.scm-manager.org"),
        LinkTextParser.parseText("www.scm-manager.org"));
    assertEquals(
        createLink("http://www.scm-manager.org", "www.scm-manager.org").concat(
          " is the home of scm-manager"), LinkTextParser.parseText(
          "www.scm-manager.org is the home of scm-manager"));
    assertEquals(
        "The home of scm-manager is ".concat(
          createLink(
            "http://www.scm-manager.org",
            "www.scm-manager.org")), LinkTextParser.parseText(
              "The home of scm-manager is www.scm-manager.org"));
    assertEquals(
        "The page ".concat(
          createLink(
            "http://www.scm-manager.org", "www.scm-manager.org").concat(
            " is the home of scm-manager")), LinkTextParser.parseText(
              "The page www.scm-manager.org is the home of scm-manager"));
  }

  /**
   * Method description
   *
   *
   * @param href
   *
   * @return
   */
  private String createLink(String href)
  {
    return MessageFormat.format("<a target=\"_blank\" href=\"{1}\">{0}</a>",
                                href, href);
  }

  /**
   * Method description
   *
   *
   * @param href
   * @param content
   *
   * @return
   */
  private String createLink(String href, String content)
  {
    return MessageFormat.format("<a target=\"_blank\" href=\"{1}\">{0}</a>",
                                content, href);
  }
}
