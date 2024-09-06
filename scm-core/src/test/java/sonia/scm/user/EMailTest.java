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

import org.junit.jupiter.api.Test;
import sonia.scm.config.ScmConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

class EMailTest {

  EMail eMail = new EMail(new ScmConfiguration());

  @Test
  void shouldUserUsersAddressIfAvailable() {
    User user = new User("dent", "Arthur Dent", "arthur@hitchhiker.com");

    String mailAddress = eMail.getMailOrFallback(user);

    assertThat(mailAddress).isEqualTo("arthur@hitchhiker.com");
  }

  @Test
  void shouldCreateAddressIfNoneAvailable() {
    User user = new User("dent", "Arthur Dent", "");

    String mailAddress = eMail.getMailOrFallback(user);

    assertThat(mailAddress).isEqualTo("dent@scm-manager.local");
  }

  @Test
  void shouldUserUsersIdIfItLooksLikeAnMailAddress() {
    User user = new User("dent@hitchhiker.com", "Arthur Dent", "");

    String mailAddress = eMail.getMailOrFallback(user);

    assertThat(mailAddress).isEqualTo("dent@hitchhiker.com");
  }
}
