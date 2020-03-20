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
    
package sonia.scm.search;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author Sebastian Sdorra
 */
public class SearchUtilTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testMultiMatchesAll()
  {
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test"), "test",
      "test hello", "hello test", "hello test hello"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test"), "test",
      "test hello", "hello test", "hello test hello", "ka"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testMultiMatchesOne()
  {
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test"), "test",
      "test hello", "hello test", "hello test hello"));
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test"), "test",
      "test hello", "hello test", "hello test hello", "ka"));
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test"), "hans", "uew",
      "klaus", "hello test hello", "ka"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSingleMatchesAll()
  {
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test"), "test"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test"), "hello test"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test"), "test hello"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test"),
      "hello test hello"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test"),
      "hello hello"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test"),
      "hello te hello"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test"),
      "hello TEST hello"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test"),
      "hello TesT hello"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSingleMatchesAllIgnoreCase()
  {
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test", true), "tEsT"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test", true),
      "heLLo teSt"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test", true),
      "TEST hellO"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test", true),
      "hEllO tEsT hEllO"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test", true),
      "heLLo heLLo"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test", true),
      "heLLo te heLLo"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSingleMatchesOne()
  {
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test"), "test"));
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test"), "hello test"));
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test"), "test hello"));
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test"),
      "hello test hello"));
    assertFalse(SearchUtil.matchesOne(new SearchRequest("test"),
      "hello hello"));
    assertFalse(SearchUtil.matchesOne(new SearchRequest("test"),
      "hello te hello"));
    assertFalse(SearchUtil.matchesOne(new SearchRequest("test"),
      "hello TEST hello"));
    assertFalse(SearchUtil.matchesOne(new SearchRequest("test"),
      "hello TesT hello"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testSingleMatchesOneIgnoreCase()
  {
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test", true), "tEsT"));
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test", true),
      "heLLo teSt"));
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test", true),
      "TEST hellO"));
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test", true),
      "hEllO tEsT hEllO"));
    assertFalse(SearchUtil.matchesOne(new SearchRequest("test", true),
      "heLLo heLLo"));
    assertFalse(SearchUtil.matchesOne(new SearchRequest("test", true),
      "heLLo te heLLo"));
  }

  /**
   * Test for issue 441
   *
   */
  @Test
  public void testSpecialCharacter()
  {
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test\\hansolo"),
      "test\\hansolo"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("*\\hansolo"),
      "test\\hansolo"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test\\*"),
      "test\\hansolo"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("test\\hansolo"),
      "abc test\\hansolo abc"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test\\hansolo"),
      "testhansolo"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test\\hansolo"),
      "test\\hnsolo"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("{test,hansolo} tst"),
      "{test,hansolo} tst"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("(test,hansolo) tst"),
      "(test,hansolo) tst"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("[test,hansolo] tst"),
      "[test,hansolo] tst"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testWildcardMatches()
  {
    assertTrue(SearchUtil.matchesAll(new SearchRequest("*test*"),
      "hello test hello"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("?es?"), "test"));
    assertTrue(SearchUtil.matchesAll(new SearchRequest("t*t"), "test"));
  }
}
