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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;
import sonia.scm.user.UserManager;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnonymousUserStartupActionTest {

  @Mock
  private ScmConfiguration scmConfiguration;
  @Mock
  private UserManager userManager;

  @InjectMocks
  private AnonymousUserStartupAction startupAction;

  @Test
  void shouldCreateAnonymousUserIfFullModeEnabled() {
    when(userManager.contains(SCMContext.USER_ANONYMOUS)).thenReturn(false);
    when(scmConfiguration.getAnonymousMode()).thenReturn(AnonymousMode.FULL);

    startupAction.run();

    verify(userManager).create(SCMContext.ANONYMOUS);
  }

  @Test
  void shouldCreateAnonymousUserIfProtocolModeEnabled() {
    when(userManager.contains(SCMContext.USER_ANONYMOUS)).thenReturn(false);
    when(scmConfiguration.getAnonymousMode()).thenReturn(AnonymousMode.PROTOCOL_ONLY);

    startupAction.run();

    verify(userManager).create(SCMContext.ANONYMOUS);
  }

  @Test
  void shouldNotCreateAnonymousUserIfNotRequired() {
    when(scmConfiguration.getAnonymousMode()).thenReturn(AnonymousMode.OFF);

    startupAction.run();

    verify(userManager, never()).create(SCMContext.ANONYMOUS);
  }

  @Test
  void shouldNotCreateAnonymousUserIfAlreadyExists() {
    when(userManager.contains(SCMContext.USER_ANONYMOUS)).thenReturn(true);
    when(scmConfiguration.getAnonymousMode()).thenReturn(AnonymousMode.FULL);

    startupAction.run();

    verify(userManager, never()).create(SCMContext.ANONYMOUS);
  }
}
