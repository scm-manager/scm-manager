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

package sonia.scm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.HandlerEventType;
import sonia.scm.search.Id;
import sonia.scm.search.Index;
import sonia.scm.search.SearchEngine;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RepositoryIndexerTest {

  @Mock
  private RepositoryManager repositoryManager;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private SearchEngine searchEngine;

  @InjectMocks
  private RepositoryIndexer indexer;

  @Test
  void shouldReturnRepositoryClass() {
    assertThat(indexer.getType()).isEqualTo(Repository.class);
  }

  @Test
  void shouldReturnVersion() {
    assertThat(indexer.getVersion()).isEqualTo(RepositoryIndexer.VERSION);
  }

  @Nested
  class UpdaterTests {

    @Mock
    private Index<Repository> index;

    private Repository repository;

    @BeforeEach
    void open() {
      when(searchEngine.forType(Repository.class).getOrCreate()).thenReturn(index);
      repository = new Repository();
      repository.setId("42");
    }

    @Test
    void shouldStoreRepository() {
      indexer.open().store(repository);

      verify(index).store(Id.of(repository), "repository:read:42", repository);
    }

    @Test
    void shouldDeleteByRepository() {
      indexer.open().delete(repository);

      verify(index).delete().byType().byRepository("42");
    }

    @Test
    void shouldReIndexAll() {
      when(repositoryManager.getAll()).thenReturn(singletonList(repository));

      indexer.open().reIndexAll();

      verify(index).delete().allTypes().byTypeName(Repository.class.getName());
      verify(index).delete().byType().all();

      verify(index).store(Id.of(repository), "repository:read:42", repository);
    }

    @Test
    void shouldHandleEvent() {
      RepositoryEvent event = new RepositoryEvent(HandlerEventType.DELETE, repository);

      indexer.handleEvent(event);

      verify(index).delete().allTypes().byRepository("42");
    }

    @Test
    void shouldCloseIndex() {
      indexer.open().close();

      verify(index).close();
    }
  }
}
