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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalToExternalUserConverterTest {

  @Mock
  ScmConfiguration scmConfiguration;

  @InjectMocks
  InternalToExternalUserConverter converter;

  @Test
  void shouldNotConvertExternalUser() {
    User external = new User();
    external.setExternal(true);

    User user = converter.convert(external);

    assertThat(user).isSameAs(external);
  }

  @Test
  void shouldNotConvertIfConfigDisabled() {
    when(scmConfiguration.isEnabledUserConverter()).thenReturn(false);
    User external = new User();
    external.setExternal(false);

    User user = converter.convert(external);

    assertThat(user).isSameAs(external);
  }

  @Test
  void shouldReturnConvertedUser() {
    when(scmConfiguration.isEnabledUserConverter()).thenReturn(true);
    User internal = new User();
    internal.setExternal(false);

    User external = converter.convert(internal);

    assertThat(external).isInstanceOf(User.class);
    assertThat(external.isExternal()).isTrue();
    assertThat(external.getPassword()).isNull();
  }
}
