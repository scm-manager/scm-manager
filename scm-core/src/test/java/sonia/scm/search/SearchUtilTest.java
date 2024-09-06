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

package sonia.scm.search;

import org.junit.Test;
import sonia.scm.TransformFilter;
import sonia.scm.user.User;

import java.util.Arrays;
import java.util.Collection;

import static java.util.stream.Collectors.toList;
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
      userList("sometester", "maintester", "anotherTester"),
      applySearchFilter(searchRequest)
    );

    assertThat(search).hasSize(2);
  }

  @Test
  public void shouldReturnAllResultsIfLimitIsNegativInt() {
    SearchRequest searchRequest = new SearchRequest("*test*", true, -1);
    Collection<User> search = SearchUtil.search(
      searchRequest,
      userList("sometester", "maintester", "anotherOne"),
      applySearchFilter(searchRequest)
    );

    assertThat(search).hasSize(2);
  }

  private Collection<User> userList(String... userNames) {
    return Arrays.stream(userNames).map(User::new).collect(toList());
  }

  private TransformFilter<User, User> applySearchFilter(SearchRequest searchRequest) {
    return item -> {
      if (SearchUtil.matchesOne(searchRequest, item.getName())) {
        return item;
      }
      return null;
    };
  }
}
