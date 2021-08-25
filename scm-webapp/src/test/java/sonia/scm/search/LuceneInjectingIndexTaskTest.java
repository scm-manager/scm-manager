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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.user.User;

import javax.inject.Inject;

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
    IndexParams params = new IndexParams("default", searchableType, IndexOptions.defaults());

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
