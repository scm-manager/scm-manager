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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ScmConfigurationTest {

  private final ScmConfiguration scmConfiguration = new ScmConfiguration();

  @Test
  void shouldReturnTrueForInitialPluginAuthUrl() {
    assertThat(scmConfiguration.isDefaultPluginAuthUrl()).isTrue();
  }

  @Test
  void shouldReturnFalseIfPluginAuthUrlHasChanged() {
    scmConfiguration.setPluginAuthUrl("https://plug.ins/oidc");
    assertThat(scmConfiguration.isDefaultPluginAuthUrl()).isFalse();
  }

  @ParameterizedTest
  @CsvSource({"https://hog.hitchiker.com/scm,scm", "https://hog.hitchiker.com/scm/,scm", "https://hog.hitchiker.com/,", "https://hog.hitchiker.com,"})
  void shouldReturnContextPath(String input, String expected) {
    scmConfiguration.setBaseUrl(input);

    assertThat(scmConfiguration.getServerContextPath()).isEqualTo(expected == null ? "" : expected);
  }
}
