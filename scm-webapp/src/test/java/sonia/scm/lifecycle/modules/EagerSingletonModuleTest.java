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

package sonia.scm.lifecycle.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;
import sonia.scm.EagerSingleton;

import static org.assertj.core.api.Assertions.assertThat;

class EagerSingletonModuleTest {

  @Test
  void shouldInitializeEagerSingletons() {
    Injector injector = Guice.createInjector(new EagerSingletonModule(), new EagerTestModule());
    injector.getInstance(EagerSingletonModule.class).initialize(injector);

    Capturer capturer = injector.getInstance(Capturer.class);
    assertThat(capturer.value).isEqualTo("eager!");
  }

  public static class EagerTestModule extends AbstractModule {

    @Override
    protected void configure() {
      bind(Capturer.class);
      bind(Eager.class);
    }
  }

  @Singleton
  public static class Capturer {
    private String value;
  }

  @EagerSingleton
  public static class Eager {

    @Inject
    public Eager(Capturer capturer) {
      capturer.value = "eager!";
    }
  }

}
