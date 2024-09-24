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

package sonia.scm.repository;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.HealthCheckFailure.templated;
import static sonia.scm.repository.HealthCheckFailure.urlForTitle;

class HealthCheckFailureTest {

  @Test
  void shouldCreateTemplatedUrl() {
    HealthCheckFailure failure = new HealthCheckFailure("1", "hyperdrive", urlForTitle("hyperdrive"), "Far too fast");

    assertThat(failure.getUrl()).isEqualTo("https://scm-manager.org/docs/latest/en/user/repo/health-checks/hyperdrive");
  }

  @Test
  void shouldCreateTemplatedUrlForGivenVersion() {
    HealthCheckFailure failure = new HealthCheckFailure("1", "hyperdrive", urlForTitle("hyperdrive"), "Far too fast");

    assertThat(failure.getUrl("1.17.x")).isEqualTo("https://scm-manager.org/docs/1.17.x/en/user/repo/health-checks/hyperdrive");
  }

  @Test
  void shouldCreateCustomTemplatedUrlForGivenVersion() {
    HealthCheckFailure failure = new HealthCheckFailure("1", "hyperdrive", templated("http://hog/{0}/error"), "Far too fast");

    assertThat(failure.getUrl("1.17.x")).isEqualTo("http://hog/1.17.x/error");
  }

  @Test
  void shouldReturnNullForUrlIfNotSet() {
    HealthCheckFailure failure = new HealthCheckFailure("1", "hyperdrive", "Far too fast");

    assertThat(failure.getUrl()).isNull();
  }
}
