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

package sonia.scm.web.cgi;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class EnvListTest {

  @Test
  void shouldNotPrintAuthorizationValue() {
    EnvList env = new EnvList();
    env.set("HTTP_AUTHORIZATION", "Basic xxx");
    env.set("SOME_OTHER", "other");
    env.set("SCM_BEARER_TOKEN", "secret");

    String value = env.toString();

    assertThat(value)
      .contains("SOME_OTHER=other")
      .contains("HTTP_AUTHORIZATION=(is set)")
      .contains("SCM_BEARER_TOKEN=(is set)")
      .doesNotContain("HTTP_AUTHORIZATION=Basic xxx")
      .doesNotContain("SCM_BEARER_TOKEN=secret");
  }

  @Test
  void shouldReturnAsArray() {
    EnvList env = new EnvList();
    env.set("SPACESHIPT", "Heart of Gold");
    env.set("DOMAIN", "hitchhiker.com");

    assertThat(env.getEnvArray())
      .contains("SPACESHIPT=Heart of Gold")
      .contains("DOMAIN=hitchhiker.com");
  }
}
