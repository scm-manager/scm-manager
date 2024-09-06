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

package sonia.scm.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sonia.scm.HandlerEventType;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AnonymousMode;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AnonymousUserDeletionEventHandlerTest {

  private ScmConfiguration scmConfiguration;

  private AnonymousUserDeletionEventHandler hook;

  @BeforeEach
  void initConfig() {
    scmConfiguration = new ScmConfiguration();
  }

  @Test
  void shouldThrowAnonymousUserDeletionExceptionIfAnonymousAccessIsEnabled() {
    scmConfiguration.setAnonymousMode(AnonymousMode.FULL);

    hook = new AnonymousUserDeletionEventHandler(scmConfiguration);
    UserEvent deletionEvent = new UserEvent(HandlerEventType.BEFORE_DELETE, SCMContext.ANONYMOUS);

    assertThrows(AnonymousUserDeletionException.class, () -> hook.onEvent(deletionEvent));
  }

  @Test
  void shouldNotThrowAnonymousUserDeletionException() {
    scmConfiguration.setAnonymousMode(AnonymousMode.OFF);

    hook = new AnonymousUserDeletionEventHandler(scmConfiguration);
    UserEvent deletionEvent = new UserEvent(HandlerEventType.BEFORE_DELETE,  SCMContext.ANONYMOUS);

    hook.onEvent(deletionEvent);
  }
}
