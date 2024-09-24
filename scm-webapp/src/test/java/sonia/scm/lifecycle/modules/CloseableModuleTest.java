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

import java.io.Closeable;

import static org.assertj.core.api.Assertions.assertThat;

class CloseableModuleTest {

  @Test
  void shouldCloseCloseables() {
    Injector injector = Guice.createInjector(new CloseableModule());
    CloseMe closeMe = injector.getInstance(CloseMe.class);

    injector.getInstance(CloseableModule.class).closeAll();
    assertThat(closeMe.closed).isTrue();
  }

  public static class CloseMe implements Closeable {

    private boolean closed = false;

    @Override
    public void close() {
      this.closed = true;
    }
  }

}
