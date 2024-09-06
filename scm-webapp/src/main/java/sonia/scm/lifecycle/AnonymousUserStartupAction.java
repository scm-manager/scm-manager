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

import jakarta.inject.Inject;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;
import sonia.scm.security.AnonymousMode;
import sonia.scm.user.UserManager;

@Extension
public class AnonymousUserStartupAction implements PrivilegedStartupAction {

  private final ScmConfiguration scmConfiguration;
  private final UserManager userManager;

  @Inject
  public AnonymousUserStartupAction(ScmConfiguration scmConfiguration, UserManager userManager) {
    this.scmConfiguration = scmConfiguration;
    this.userManager = userManager;
  }

  @Override
  public void run() {
    if (anonymousUserRequiredButNotExists()) {
      userManager.create(SCMContext.ANONYMOUS);
    }
  }

  private boolean anonymousUserRequiredButNotExists() {
    return scmConfiguration.getAnonymousMode() != AnonymousMode.OFF && !userManager.contains(SCMContext.USER_ANONYMOUS);
  }
}
