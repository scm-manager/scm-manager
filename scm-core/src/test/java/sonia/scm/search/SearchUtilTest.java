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

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import sonia.scm.user.User;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchUtilTest {

  @Test
  public void testMultiMatchesAll() {
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "test", "test hello", "hello test", "hello test hello")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "test", "test hello", "hello test", "hello test hello", "ka")).isFalse();
  }

  @Test
  public void testMultiMatchesOne() {
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "test", "test hello", "hello test", "hello test hello")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "test", "test hello", "hello test", "hello test hello", "ka")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "hans", "uew", "klaus", "hello test hello", "ka")).isTrue();
  }

  @Test
  public void testSingleMatchesAll() {
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "test")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "hello test")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "test hello")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "hello test hello")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "hello hello")).isFalse();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "hello te hello")).isFalse();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "hello TEST hello")).isFalse();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test"), "hello TesT hello")).isFalse();
  }

  @Test
  public void testSingleMatchesAllIgnoreCase() {
    assertThat(SearchUtil.matchesAll(new SearchRequest("test", true), "tEsT")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test", true), "heLLo teSt")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test", true), "TEST hellO")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test", true), "hEllO tEsT hEllO")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test", true), "heLLo heLLo")).isFalse();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test", true), "heLLo te heLLo")).isFalse();
  }

  @Test
  public void testSingleMatchesOne() {
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "test")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "hello test")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "test hello")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "hello test hello")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "hello hello")).isFalse();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "hello te hello")).isFalse();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "hello TEST hello")).isFalse();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test"), "hello TesT hello")).isFalse();
  }

  @Test
  public void testSingleMatchesOneIgnoreCase() {
    assertThat(SearchUtil.matchesOne(new SearchRequest("test", true), "tEsT")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test", true), "heLLo teSt")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test", true), "TEST hellO")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test", true), "hEllO tEsT hEllO")).isTrue();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test", true), "heLLo heLLo")).isFalse();
    assertThat(SearchUtil.matchesOne(new SearchRequest("test", true), "heLLo te heLLo")).isFalse();
  }

  /**
   * Test for issue 441
   */
  @Test
  public void testSpecialCharacter() {
    assertThat(SearchUtil.matchesAll(new SearchRequest("test\\hansolo"), "test\\hansolo")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("*\\hansolo"), "test\\hansolo")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test\\*"), "test\\hansolo")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test\\hansolo"), "abc test\\hansolo abc")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test\\hansolo"), "testhansolo")).isFalse();
    assertThat(SearchUtil.matchesAll(new SearchRequest("test\\hansolo"), "test\\hnsolo")).isFalse();
    assertThat(SearchUtil.matchesAll(new SearchRequest("{test,hansolo} tst"), "{test,hansolo} tst")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("(test,hansolo) tst"), "(test,hansolo) tst")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("[test,hansolo] tst"), "[test,hansolo] tst")).isTrue();
  }

  @Test
  public void testWildcardMatches() {
    assertThat(SearchUtil.matchesAll(new SearchRequest("*test*"), "hello test hello")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("?es?"), "test")).isTrue();
    assertThat(SearchUtil.matchesAll(new SearchRequest("t*t"), "test")).isTrue();
  }

  @Test
  public void shouldReturnLimitedResults() {
    SearchRequest searchRequest = new SearchRequest("*test*", true, 2);
    Collection<User> search = SearchUtil.search(
      searchRequest,
      ImmutableList.of(
        new User("sometester"),
        new User("maintester"),
        new User("anothertester")
      ),
      item -> {
        User user = null;
        if (SearchUtil.matchesOne(searchRequest, item.getName())) {
          user = item;
        }
        return user;
      }
    );

    assertThat(search).hasSize(2);
  }

  @Test
  public void shouldReturnAllResultsIfLimitIsNegativInt() {
    SearchRequest searchRequest = new SearchRequest("*test*", true, -1);
    Collection<User> search = SearchUtil.search(
      searchRequest,
      ImmutableList.of(
        new User("sometester"),
        new User("maintester"),
        new User("anotherOne")
      ),
      item -> {
        User user = null;
        if (SearchUtil.matchesOne(searchRequest, item.getName())) {
          user = item;
        }
        return user;
      }
    );

    assertThat(search).hasSize(2);
  }
}
