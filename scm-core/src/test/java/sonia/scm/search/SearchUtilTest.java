/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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
                                     "test hello", "hello test",
                                     "hello test hello"));
    assertFalse(SearchUtil.matchesAll(new SearchRequest("test"), "test",
                                      "test hello", "hello test",
                                      "hello test hello", "ka"));
  }

  /**
   * Method description
   *
   */
  @Test
  public void testMultiMatchesOne()
  {
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test"), "test",
                                     "test hello", "hello test",
                                     "hello test hello"));
    assertTrue(SearchUtil.matchesOne(new SearchRequest("test"), "test",
                                     "test hello", "hello test",
                                     "hello test hello", "ka"));
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
