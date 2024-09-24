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
