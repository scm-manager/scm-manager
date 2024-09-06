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

import com.google.common.base.Strings;
import jakarta.inject.Inject;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.ValidationUtil;

/**
 * Email is able to resolve email addresses of users.
 *
 * @since 2.8.0
 */
public class EMail {

  private final ScmConfiguration scmConfiguration;

  @Inject
  public EMail(ScmConfiguration scmConfiguration) {
    this.scmConfiguration = scmConfiguration;
  }

  /**
   * Returns the email address of the given user or a generated fallback address.
   * @param user user to resolve address from
   * @return email address or fallback
   */
  public String getMailOrFallback(User user) {
    return getMailOrFallback(DisplayUser.from(user));
  }

  /**
   * Returns the email address of the given user or a generated fallback address.
   * @param user user to resolve address from
   * @return email address or fallback
   */
  public String getMailOrFallback(DisplayUser user) {
    if (Strings.isNullOrEmpty(user.getMail())) {
      if (isMailUsedAsId(user)) {
        return user.getId();
      } else {
        return createFallbackMail(user);
      }
    } else {
      return user.getMail();
    }
  }

  private boolean isMailUsedAsId(DisplayUser user) {
    return ValidationUtil.isMailAddressValid(user.getId());
  }

  private String createFallbackMail(DisplayUser user) {
    return user.getId() + "@" + scmConfiguration.getMailDomainName();
  }
}
