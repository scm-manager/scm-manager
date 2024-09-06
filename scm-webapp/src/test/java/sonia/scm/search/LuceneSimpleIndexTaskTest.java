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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;

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

    LuceneSearchableType searchableType = resolver.resolve(Repository.class);

    IndexParams params = new IndexParams("default", searchableType);

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
