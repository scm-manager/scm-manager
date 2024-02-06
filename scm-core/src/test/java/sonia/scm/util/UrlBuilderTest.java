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
