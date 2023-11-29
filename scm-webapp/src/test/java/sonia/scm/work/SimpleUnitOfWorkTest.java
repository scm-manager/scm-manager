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

package sonia.scm.work;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jakarta.inject.Inject;
import lombok.Value;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleUnitOfWorkTest {

  private PrincipalCollection principal = new SimplePrincipalCollection("trillian", "test");

  @Test
  void shouldInjectMember() {
    Context context = new Context("awesome");
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Context.class).toInstance(context);
      }
    });

    SimpleTask simpleTask = new SimpleTask();
    SimpleUnitOfWork unitOfWork = new SimpleUnitOfWork(1L, principal, Collections.emptySet(), simpleTask);
    unitOfWork.task(injector);

    simpleTask.run();

    assertThat(simpleTask.value).isEqualTo("awesome");
  }

  @Value
  public class Context {
    String value;
  }

  public class SimpleTask implements Task {

    private Context context;

    private String value = "no value set";

    @Inject
    public void setContext(Context context) {
      this.context = context;
    }

    @Override
    public void run() {
      if (context != null) {
        value = context.getValue();
      }
    }
  }

}
