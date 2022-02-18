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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LuceneSimpleIndexTaskTest {

  @Mock
  private LuceneIndexFactory indexFactory;

  @Mock
  private LuceneIndex<Repository> index;

  private final SearchableTypeResolver resolver = new SearchableTypeResolver(Repository.class);

  @Test
  void shouldUpdate() {
    Injector injector = createInjector();

    Optional<LuceneSearchableType> searchableType = resolver.resolve(Repository.class);

    IndexParams params = new IndexParams("default", searchableType.orElse(null));

    AtomicReference<Index<?>> ref = new AtomicReference<>();
    LuceneSimpleIndexTask task = new LuceneSimpleIndexTask(params, ref::set);
    injector.injectMembers(task);

    when(indexFactory.create(params)).then(ic -> index);

    task.run();

    assertThat(index).isSameAs(ref.get());
    verify(index).close();
  }

  private Injector createInjector() {
    return Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(SearchableTypeResolver.class).toInstance(resolver);
        bind(LuceneIndexFactory.class).toInstance(indexFactory);
      }
    });
  }

}
