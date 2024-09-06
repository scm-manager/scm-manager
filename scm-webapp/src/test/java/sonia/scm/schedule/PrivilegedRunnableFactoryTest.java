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

package sonia.scm.schedule;

import com.google.inject.util.Providers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.web.security.AdministrationContext;
import sonia.scm.web.security.PrivilegedAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class PrivilegedRunnableFactoryTest {

  @Mock
  private AdministrationContext administrationContext;

  @InjectMocks
  private PrivilegedRunnableFactory runnableFactory;

  @Test
  void shouldRunAsPrivilegedAction() {
    doAnswer((ic) -> {
      PrivilegedAction action = ic.getArgument(0);
      action.run();
      return null;
    }).when(administrationContext).runAsAdmin(any(PrivilegedAction.class));

    RemindingRunnable runnable = new RemindingRunnable();

    Runnable action = runnableFactory.create(Providers.of(runnable));
    assertThat(action).isNotExactlyInstanceOf(RemindingRunnable.class);

    assertThat(runnable.run).isFalse();
    action.run();
    assertThat(runnable.run).isTrue();
  }

  private static class RemindingRunnable implements Runnable {

    private boolean run = false;

    @Override
    public void run() {
      run = true;
    }
  }

}
