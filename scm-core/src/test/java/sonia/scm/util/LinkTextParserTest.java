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
    
package sonia.scm.util;


import org.junit.Test;

import static org.junit.Assert.*;

import java.text.MessageFormat;


public class LinkTextParserTest
{

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


  private String createLink(String href)
  {
    return MessageFormat.format("<a target=\"_blank\" href=\"{1}\">{0}</a>",
                                href, href);
  }


  private String createLink(String href, String content)
  {
    return MessageFormat.format("<a target=\"_blank\" href=\"{1}\">{0}</a>",
                                content, href);
  }
}
