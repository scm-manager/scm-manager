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

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.plugin.Extension;

@Slf4j
@Extension
public class InternalToExternalUserConverter implements ExternalUserConverter{

  private final ScmConfiguration scmConfiguration;

  @Inject
  public InternalToExternalUserConverter(ScmConfiguration scmConfiguration) {
    this.scmConfiguration = scmConfiguration;
  }

  public User convert(User user) {
    if (shouldConvertUser(user)) {
      log.info("Convert internal user {} to external", user.getId());
      user.setExternal(true);
      user.setPassword(null);
    }
    return user;
  }

  private boolean shouldConvertUser(User user) {
    return !user.isExternal() && scmConfiguration.isEnabledUserConverter();
  }
}
