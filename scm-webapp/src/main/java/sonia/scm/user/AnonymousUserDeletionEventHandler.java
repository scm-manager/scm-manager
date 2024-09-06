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

import com.github.legman.Subscribe;
import jakarta.inject.Inject;
import sonia.scm.ContextEntry;
import sonia.scm.EagerSingleton;
import sonia.scm.HandlerEventType;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AnonymousMode;

@EagerSingleton
@Extension
public class AnonymousUserDeletionEventHandler {

  private final ScmConfiguration scmConfiguration;

  @Inject
  public AnonymousUserDeletionEventHandler(ScmConfiguration scmConfiguration) {
    this.scmConfiguration = scmConfiguration;
  }

  @Subscribe(async = false)
  public void onEvent(UserEvent event) {
    if (isAnonymousUserDeletionNotAllowed(event)) {
      throw new AnonymousUserDeletionException(ContextEntry.ContextBuilder.entity(User.class, event.getItem().getId()));
    }
  }

  private boolean isAnonymousUserDeletionNotAllowed(UserEvent event) {
    return event.getEventType() == HandlerEventType.BEFORE_DELETE
      && event.getItem().getName().equals(SCMContext.USER_ANONYMOUS)
      && scmConfiguration.getAnonymousMode() != AnonymousMode.OFF;
  }
}
