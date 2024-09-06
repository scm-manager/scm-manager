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
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.user.User;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LuceneInjectingIndexTaskTest {

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private LuceneIndexFactory indexFactory;

  private static String capturedValue;

  private final SearchableTypeResolver resolver = new SearchableTypeResolver(User.class);

  @BeforeEach
  void cleanUp() {
    capturedValue = "notAsExpected";
  }

  @Test
  void shouldInjectAndUpdate() {
    Injector injector = createInjector();

    LuceneSearchableType searchableType = resolver.resolve(User.class);
    IndexParams params = new IndexParams("default", searchableType);

    LuceneInjectingIndexTask task = new LuceneInjectingIndexTask(params, InjectingTask.class);
    injector.injectMembers(task);

    task.run();

    assertThat(capturedValue).isEqualTo("runAsExpected");
  }

  private Injector createInjector() {
    return Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(SearchableTypeResolver.class).toInstance(resolver);
        bind(LuceneIndexFactory.class).toInstance(indexFactory);
        bind(String.class).toInstance("runAsExpected");
      }
    });
  }

  public static class InjectingTask implements IndexTask<String> {

    private final String value;

    @Inject
    public InjectingTask(String value) {
      this.value = value;
    }

    @Override
    public void update(Index<String> index) {
      capturedValue = value;
    }
  }

}
