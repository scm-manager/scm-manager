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

package sonia.scm.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AnonymousMode;
import sonia.scm.user.UserManager;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScmConfigurationChangedListenerTest {

  @Mock
  private UserManager userManager;

  private final ScmConfiguration scmConfiguration = new ScmConfiguration();

  private ScmConfigurationChangedListener listener;

  @BeforeEach
  void initListener() {
    listener = new ScmConfigurationChangedListener(userManager);
  }

  @Test
  void shouldCreateAnonymousUserIfAnonymousAccessEnabled() {
    when(userManager.contains(any())).thenReturn(false);

    ScmConfiguration changes = new ScmConfiguration();
    changes.setAnonymousMode(AnonymousMode.FULL);
    scmConfiguration.load(changes);

    listener.handleEvent(new ScmConfigurationChangedEvent(scmConfiguration));
    verify(userManager).create(any());
  }

  @Test
  void shouldNotCreateAnonymousUserIfAlreadyExists() {
    when(userManager.contains(any())).thenReturn(true);

    ScmConfiguration changes = new ScmConfiguration();
    changes.setAnonymousMode(AnonymousMode.FULL);
    scmConfiguration.load(changes);

    listener.handleEvent(new ScmConfigurationChangedEvent(scmConfiguration));
    verify(userManager, never()).create(any());
  }

  @Test
  void shouldNotCreateAnonymousUserIfAnonymousAccessDisabled() {
    //anonymous access disabled by default
     listener.handleEvent(new ScmConfigurationChangedEvent(scmConfiguration));
    verify(userManager, never()).create(any());
  }
}
