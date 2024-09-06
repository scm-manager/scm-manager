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
