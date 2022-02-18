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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.user.User;
import sonia.scm.work.CentralWorkQueue;
import sonia.scm.work.Task;

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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SubjectAware("trillian")
@ExtendWith({MockitoExtension.class, ShiroExtension.class})
class LuceneSearchEngineTest {

  @Mock
  private IndexManager indexManager;

  @Mock
  private SearchableTypeResolver resolver;

  @Mock
  private LuceneQueryBuilderFactory queryBuilderFactory;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private CentralWorkQueue centralWorkQueue;

  @InjectMocks
  private LuceneSearchEngine searchEngine;

  @Mock
  private LuceneSearchableType searchableType;

  @Nested
  class GetSearchableTypesTests {

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
  }

  @Nested
  class SearchTests {

    @Test
    @SuppressWarnings("unchecked")
    void shouldDelegateSearchWithDefaults() {
      LuceneQueryBuilder<Repository> mockedBuilder = mock(LuceneQueryBuilder.class);
      when(resolver.resolve(Repository.class)).thenReturn(Optional.of(searchableType));

      IndexParams params = new IndexParams("default", searchableType);
      when(queryBuilderFactory.<Repository>create(params)).thenReturn(mockedBuilder);

      QueryBuilder<Repository> queryBuilder = searchEngine.forType(Repository.class).search();

      assertThat(queryBuilder).isSameAs(mockedBuilder);
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldDelegateSearch() {
      LuceneQueryBuilder<Repository> mockedBuilder = mock(LuceneQueryBuilder.class);
      when(resolver.resolve(Repository.class)).thenReturn(Optional.of(searchableType));

      IndexParams params = new IndexParams("idx", searchableType);
      when(queryBuilderFactory.<Repository>create(params)).thenReturn(mockedBuilder);

      QueryBuilder<Repository> queryBuilder = searchEngine.forType(Repository.class).withIndex("idx").search();

      assertThat(queryBuilder).isSameAs(mockedBuilder);
    }

    @Test
    void shouldFailWithoutRequiredPermission() {
      when(searchableType.getPermission()).thenReturn(Optional.of("repository:read"));
      when(resolver.resolve(Repository.class)).thenReturn(Optional.of(searchableType));

      SearchEngine.ForType<Repository> forType = searchEngine.forType(Repository.class);
      assertThrows(AuthorizationException.class, forType::search);
    }

    @Test
    @SuppressWarnings("unchecked")
    @SubjectAware(permissions = "repository:read")
    void shouldNotFailWithRequiredPermission() {
      when(searchableType.getPermission()).thenReturn(Optional.of("repository:read"));
      when(resolver.resolve(Repository.class)).thenReturn(Optional.of(searchableType));

      LuceneQueryBuilder<Object> mockedBuilder = mock(LuceneQueryBuilder.class);
      when(queryBuilderFactory.create(any())).thenReturn(mockedBuilder);

      SearchEngine.ForType<Repository> forType = searchEngine.forType(Repository.class);
      assertThat(forType.search()).isNotNull();
    }

    @Test
    void shouldFailWithTypeNameWithoutRequiredPermission() {
      when(searchableType.getPermission()).thenReturn(Optional.of("repository:read"));
      when(resolver.resolveByName("repository")).thenReturn(Optional.of(searchableType));

      SearchEngine.ForType<Object> forType = searchEngine.forType("repository");
      assertThrows(AuthorizationException.class, forType::search);
    }

    @Test
    @SuppressWarnings("unchecked")
    @SubjectAware(permissions = "repository:read")
    void shouldNotFailWithTypeNameAndRequiredPermission() {
      when(searchableType.getPermission()).thenReturn(Optional.of("repository:read"));
      when(resolver.resolveByName("repository")).thenReturn(Optional.of(searchableType));

      LuceneQueryBuilder<Object> mockedBuilder = mock(LuceneQueryBuilder.class);
      when(queryBuilderFactory.create(any())).thenReturn(mockedBuilder);

      SearchEngine.ForType<Object> forType = searchEngine.forType("repository");
      assertThat(forType.search()).isNotNull();
    }
  }

  @Nested
  class IndexTests {

    @Captor
    private ArgumentCaptor<Task> taskCaptor;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CentralWorkQueue.Enqueue enqueue;

    @BeforeEach
    void setUp() {
      when(centralWorkQueue.append()).thenReturn(enqueue);
    }

    @Test
    void shouldSubmitSimpleTask() {
      mockType();

      searchEngine.forType(Repository.class).update(index -> {});

      verifyTaskSubmitted(LuceneSimpleIndexTask.class);
    }

    @Test
    void shouldSubmitInjectingTask() {
      mockType();

      searchEngine.forType(Repository.class).update(DummyIndexTask.class);

      verifyTaskSubmitted(LuceneInjectingIndexTask.class);
    }

    @Test
    void shouldLockTypeAndDefaultIndex() {
      mockType();

      searchEngine.forType(Repository.class).update(DummyIndexTask.class);

      verify(enqueue).locks("repository-default-index");
    }

    @Test
    void shouldLockTypeAndIndex() {
      mockType();

      searchEngine.forType(Repository.class).withIndex("sample").update(DummyIndexTask.class);

      verify(enqueue).locks("repository-sample-index");
    }

    @Test
    void shouldLockSpecificResource() {
      mockType();

      searchEngine.forType(Repository.class).forResource("one").update(DummyIndexTask.class);

      verify(enqueue).locks("repository-default-index", "one");
    }

    @Test
    void shouldLockMultipleSpecificResources() {
      mockType();

      searchEngine.forType(Repository.class)
        .forResource("one")
        .forResource("two")
        .update(DummyIndexTask.class);

      verify(enqueue).locks("repository-default-index", "one");
      verify(enqueue).locks("repository-default-index", "two");
    }

    @Test
    void shouldBatchSimpleTask() {
      mockDetails(new LuceneIndexDetails(Repository.class, "default"));

      searchEngine.forIndices().batch(index -> {});

      verifyTaskSubmitted(LuceneSimpleIndexTask.class);
    }

    @Test
    void shouldBatchAndLock() {
      mockDetails(new LuceneIndexDetails(Repository.class, "default"));

      searchEngine.forIndices().batch(index -> {});

      verify(enqueue).locks("repository-default-index");
    }

    @Test
    void shouldBatchAndLockSpecificResource() {
      mockDetails(new LuceneIndexDetails(Repository.class, "default"));

      searchEngine.forIndices().forResource("one").batch(index -> {});

      verify(enqueue).locks("repository-default-index", "one");
    }

    @Test
    void shouldBatchAndLockMultipleSpecificResources() {
      mockDetails(new LuceneIndexDetails(Repository.class, "default"));

      searchEngine.forIndices().forResource("one").forResource("two").batch(index -> {});

      verify(enqueue).locks("repository-default-index", "one");
      verify(enqueue).locks("repository-default-index", "two");
    }

    @Test
    void shouldBatchInjectingTask() {
      mockDetails(new LuceneIndexDetails(Repository.class, "default"));

      searchEngine.forIndices().batch(DummyIndexTask.class);

      verifyTaskSubmitted(LuceneInjectingIndexTask.class);
    }

    @Test
    void shouldBatchMultipleTasks() {
      mockDetails(
        new LuceneIndexDetails(Repository.class, "default"),
        new LuceneIndexDetails(User.class, "default")
      );

      searchEngine.forIndices().batch(index -> {});

      verify(enqueue.runAsAdmin(), times(2)).enqueue(any(Task.class));
    }

    @Test
    void shouldFilterWithPredicate() {
      mockDetails(
        new LuceneIndexDetails(Repository.class, "default"),
        new LuceneIndexDetails(User.class, "default")
      );

      searchEngine.forIndices()
        .matching(details -> details.getType() == Repository.class)
        .batch(index -> {});

      verify(enqueue.runAsAdmin()).enqueue(any(Task.class));
    }

    private <T extends IndexDetails> void mockDetails(LuceneIndexDetails... details) {
      for (LuceneIndexDetails detail : details) {
        mockType(detail.getType());
      }
      when(indexManager.all()).thenAnswer(ic -> Arrays.asList(details));
    }

    private void verifyTaskSubmitted(Class<? extends Task> typeOfTask) {
      verify(enqueue.runAsAdmin()).enqueue(taskCaptor.capture());

      Task task = taskCaptor.getValue();
      assertThat(task).isInstanceOf(typeOfTask);
    }

    private void mockType() {
      mockType(Repository.class);
    }

    private void mockType(Class<?> type){
      LuceneSearchableType searchableType = mock(LuceneSearchableType.class);
      lenient().when(searchableType.getType()).thenAnswer(ic -> type);
      lenient().when(searchableType.getName()).thenReturn(type.getSimpleName().toLowerCase(Locale.ENGLISH));
      lenient().when(resolver.resolve(type)).thenReturn(Optional.of(searchableType));
    }

  }


  public static class DummyIndexTask implements IndexTask<Repository> {

    @Override
    public void update(Index<Repository> index) {

    }
  }

}
