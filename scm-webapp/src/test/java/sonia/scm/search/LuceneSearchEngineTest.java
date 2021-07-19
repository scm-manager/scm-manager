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

import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SubjectAware("trillian")
@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class LuceneSearchEngineTest {

  @Mock
  private SearchableTypeResolver resolver;

  @Mock
  private LuceneIndexFactory indexFactory;

  @Mock
  private LuceneQueryBuilderFactory queryBuilderFactory;

  @InjectMocks
  private LuceneSearchEngine searchEngine;

  @Test
  void shouldDelegateGetSearchableTypes() {
    List<LuceneSearchableType> mockedTypes = Collections.singletonList(searchableType("repository"));
    when(resolver.getSearchableTypes()).thenReturn(mockedTypes);

    List<SearchableType> searchableTypes = searchEngine.getSearchableTypes();

    assertThat(searchableTypes).containsAll(mockedTypes);
  }

  @Test
  @SubjectAware(value = "dent", permissions = "user:list")
  void shouldExcludeTypesWithoutPermission() {
    LuceneSearchableType repository = searchableType("repository");
    LuceneSearchableType user = searchableType("user", "user:list");
    LuceneSearchableType group = searchableType("group", "group:list");
    List<LuceneSearchableType> mockedTypes = Arrays.asList(repository, user, group);
    when(resolver.getSearchableTypes()).thenReturn(mockedTypes);

    List<SearchableType> searchableTypes = searchEngine.getSearchableTypes();

    assertThat(searchableTypes).containsOnly(repository, user);
  }

  private LuceneSearchableType searchableType(String name) {
    return searchableType(name, null);
  }

  private LuceneSearchableType searchableType(String name, String permission) {
    LuceneSearchableType searchableType = mock(LuceneSearchableType.class);
    lenient().when(searchableType.getName()).thenReturn(name);
    when(searchableType.getPermission()).thenReturn(Optional.ofNullable(permission));
    return searchableType;
  }

  @Test
  void shouldDelegateGetOrCreateIndexWithDefaults() {
    LuceneIndex index = mock(LuceneIndex.class);
    when(indexFactory.create("idx", IndexOptions.defaults())).thenReturn(index);

    Index idx = searchEngine.getOrCreate("idx");
    assertThat(idx).isSameAs(index);
  }

  @Test
  void shouldDelegateGetOrCreateIndex() {
    LuceneIndex index = mock(LuceneIndex.class);
    IndexOptions options = IndexOptions.naturalLanguage(Locale.ENGLISH);
    when(indexFactory.create("idx", options)).thenReturn(index);

    Index idx = searchEngine.getOrCreate("idx", options);
    assertThat(idx).isSameAs(index);
  }

  @Test
  void shouldDelegateSearchWithDefaults() {
    LuceneQueryBuilder mockedBuilder = mock(LuceneQueryBuilder.class);
    when(queryBuilderFactory.create("idx", IndexOptions.defaults())).thenReturn(mockedBuilder);

    QueryBuilder queryBuilder = searchEngine.search("idx");

    assertThat(queryBuilder).isSameAs(mockedBuilder);
  }

  @Test
  void shouldDelegateSearch() {
    LuceneQueryBuilder mockedBuilder = mock(LuceneQueryBuilder.class);
    IndexOptions options = IndexOptions.naturalLanguage(Locale.GERMAN);
    when(queryBuilderFactory.create("idx", options)).thenReturn(mockedBuilder);

    QueryBuilder queryBuilder = searchEngine.search("idx", options);

    assertThat(queryBuilder).isSameAs(mockedBuilder);
  }

}
