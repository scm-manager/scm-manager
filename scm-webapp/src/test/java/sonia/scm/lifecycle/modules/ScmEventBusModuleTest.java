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

import com.github.legman.Subscribe;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import sonia.scm.event.LegmanScmEventBus;

import static org.assertj.core.api.Assertions.assertThat;

class ScmEventBusModuleTest {

  @Test
  void shouldRegisterInstance() {
    LegmanScmEventBus eventBus = new LegmanScmEventBus();

    Injector injector = Guice.createInjector(new ScmEventBusModule(eventBus));
    Listener listener = injector.getInstance(Listener.class);

    eventBus.post("hello");

    assertThat(listener.message).isEqualTo("hello");
  }

  public static class Listener {

    private String message;

    @Subscribe(async = false)
    public void receive(String message) {
      this.message = message;
    }

  }
}
