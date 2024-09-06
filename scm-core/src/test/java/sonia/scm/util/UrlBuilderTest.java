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


public class UrlBuilderTest
{

   @Test
  public void testAppendParameter()
  {
    UrlBuilder builder = new UrlBuilder("http://www.short.de");

    builder.appendParameter("i", 123).appendParameter("s", "abc");
    builder.appendParameter("b", true).appendParameter("l", 321L);
    assertEquals("http://www.short.de?i=123&s=abc&b=true&l=321", builder.toString());
    builder.appendParameter("c", "a b");
    assertEquals("http://www.short.de?i=123&s=abc&b=true&l=321&c=a%20b", builder.toString());
  }

   @Test
  public void testAppendUrlPart()
  {
    UrlBuilder builder = new UrlBuilder("http://www.short.de");

    builder.appendUrlPart("test");
    assertEquals("http://www.short.de/test", builder.toString());
  }

   @Test(expected = IllegalStateException.class)
  public void testState()
  {
    UrlBuilder builder = new UrlBuilder("http://www.short.de");

    builder.appendParameter("test", "123").appendUrlPart("test");
  }
}
