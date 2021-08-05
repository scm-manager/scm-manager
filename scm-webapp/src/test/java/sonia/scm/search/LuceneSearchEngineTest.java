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

import org.apache.shiro.authz.AuthorizationException;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SubjectAware("trillian")
@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class LuceneSearchEngineTest {

  @Mock
  private SearchableTypeResolver resolver;

  @Mock
  private IndexQueue indexQueue;

  @Mock
  private LuceneQueryBuilderFactory queryBuilderFactory;

  @InjectMocks
  private LuceneSearchEngine searchEngine;

  @Mock
  private LuceneSearchableType searchableType;

  @Test
  void shouldDelegateGetSearchableTypes() {
    List<LuceneSearchableType> mockedTypes = Collections.singletonList(searchableType("repository"));
    when(resolver.getSearchableTypes()).thenReturn(mockedTypes);

    Collection<SearchableType> searchableTypes = searchEngine.getSearchableTypes();

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

    Collection<SearchableType> searchableTypes = searchEngine.getSearchableTypes();

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
  @SuppressWarnings("unchecked")
  void shouldDelegateGetOrCreateWithDefaultIndex() {
    Index<Repository> index = mock(Index.class);

    when(resolver.resolve(Repository.class)).thenReturn(searchableType);
    IndexParams params = new IndexParams("default", searchableType, IndexOptions.defaults());
    when(indexQueue.<Repository>getQueuedIndex(params)).thenReturn(index);

    Index<Repository> idx = searchEngine.forType(Repository.class).getOrCreate();
    assertThat(idx).isSameAs(index);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldDelegateGetOrCreateIndexWithDefaults() {
    Index<Repository> index = mock(Index.class);

    when(resolver.resolve(Repository.class)).thenReturn(searchableType);
    IndexParams params = new IndexParams("idx", searchableType, IndexOptions.defaults());
    when(indexQueue.<Repository>getQueuedIndex(params)).thenReturn(index);

    Index<Repository> idx = searchEngine.forType(Repository.class).withIndex("idx").getOrCreate();
    assertThat(idx).isSameAs(index);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldDelegateGetOrCreateIndex() {
    Index<Repository> index = mock(Index.class);
    IndexOptions options = IndexOptions.naturalLanguage(Locale.ENGLISH);

    when(resolver.resolve(Repository.class)).thenReturn(searchableType);
    IndexParams params = new IndexParams("default", searchableType, options);
    when(indexQueue.<Repository>getQueuedIndex(params)).thenReturn(index);

    Index<Repository> idx = searchEngine.forType(Repository.class).withOptions(options).getOrCreate();
    assertThat(idx).isSameAs(index);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldDelegateSearchWithDefaults() {
    LuceneQueryBuilder<Repository> mockedBuilder = mock(LuceneQueryBuilder.class);
    when(resolver.resolve(Repository.class)).thenReturn(searchableType);

    IndexParams params = new IndexParams("default", searchableType, IndexOptions.defaults());
    when(queryBuilderFactory.<Repository>create(params)).thenReturn(mockedBuilder);

    QueryBuilder<Repository> queryBuilder = searchEngine.forType(Repository.class).search();

    assertThat(queryBuilder).isSameAs(mockedBuilder);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldDelegateSearch() {
    IndexOptions options = IndexOptions.naturalLanguage(Locale.GERMAN);

    LuceneQueryBuilder<Repository> mockedBuilder = mock(LuceneQueryBuilder.class);
    when(resolver.resolve(Repository.class)).thenReturn(searchableType);

    IndexParams params = new IndexParams("idx", searchableType, options);
    when(queryBuilderFactory.<Repository>create(params)).thenReturn(mockedBuilder);

    QueryBuilder<Repository> queryBuilder = searchEngine.forType(Repository.class).withIndex("idx").withOptions(options).search();

    assertThat(queryBuilder).isSameAs(mockedBuilder);
  }

  @Test
  void shouldFailWithoutRequiredPermission() {
    when(searchableType.getPermission()).thenReturn(Optional.of("repository:read"));
    when(resolver.resolve(Repository.class)).thenReturn(searchableType);

    SearchEngine.ForType<Repository> forType = searchEngine.forType(Repository.class);
    assertThrows(AuthorizationException.class, forType::search);
  }

  @Test
  @SuppressWarnings("unchecked")
  @SubjectAware(permissions = "repository:read")
  void shouldNotFailWithRequiredPermission() {
    when(searchableType.getPermission()).thenReturn(Optional.of("repository:read"));
    when(resolver.resolve(Repository.class)).thenReturn(searchableType);

    LuceneQueryBuilder<Object> mockedBuilder = mock(LuceneQueryBuilder.class);
    when(queryBuilderFactory.create(any())).thenReturn(mockedBuilder);

    SearchEngine.ForType<Repository> forType = searchEngine.forType(Repository.class);
    assertThat(forType.search()).isNotNull();
  }

  @Test
  void shouldFailWithTypeNameWithoutRequiredPermission() {
    when(searchableType.getPermission()).thenReturn(Optional.of("repository:read"));
    when(resolver.resolveByName("repository")).thenReturn(searchableType);

    SearchEngine.ForType<Object> forType = searchEngine.forType("repository");
    assertThrows(AuthorizationException.class, forType::search);
  }

  @Test
  @SuppressWarnings("unchecked")
  @SubjectAware(permissions = "repository:read")
  void shouldNotFailWithTypeNameAndRequiredPermission() {
    when(searchableType.getPermission()).thenReturn(Optional.of("repository:read"));
    when(resolver.resolveByName("repository")).thenReturn(searchableType);

    LuceneQueryBuilder<Object> mockedBuilder = mock(LuceneQueryBuilder.class);
    when(queryBuilderFactory.create(any())).thenReturn(mockedBuilder);

    SearchEngine.ForType<Object> forType = searchEngine.forType("repository");
    assertThat(forType.search()).isNotNull();
  }

}
