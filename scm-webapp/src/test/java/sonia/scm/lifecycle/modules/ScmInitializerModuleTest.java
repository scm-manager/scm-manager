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

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import sonia.scm.Initable;
import sonia.scm.SCMContextProvider;

import static org.assertj.core.api.Assertions.assertThat;

class ScmInitializerModuleTest {

  @Test
  void shouldInitializeInstances() {
    Injector injector = Guice.createInjector(new ScmInitializerModule());
    InitializeMe instance = injector.getInstance(InitializeMe.class);

    assertThat(instance.initialized).isTrue();
  }

  public static class InitializeMe implements Initable {

    private boolean initialized = false;

    @Override
    public void init(SCMContextProvider context) {
      this.initialized = true;
    }
  }

}
