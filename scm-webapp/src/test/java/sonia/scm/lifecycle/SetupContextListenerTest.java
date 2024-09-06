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

package sonia.scm.lifecycle;

import com.google.common.collect.ImmutableSet;
import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.web.security.AdministrationContext;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SetupContextListenerTest {

  @Mock
  private AdministrationContext administrationContext;

  private SetupContextListener setupContextListener;

  @BeforeEach
  void initSetupContextListener() {
    Set<PrivilegedStartupAction> startupActions = ImmutableSet.of(() -> {}, () -> {});
    setupContextListener = new SetupContextListener(startupActions, administrationContext);
  }

  @Test
  void shouldRunStartupActionsWithAdministrationContext() {
    ServletContextEvent contextEvent = mock(ServletContextEvent.class);

    setupContextListener.contextInitialized(contextEvent);

    verify(administrationContext, times(2)).runAsAdmin(any(PrivilegedStartupAction.class));
  }
}
