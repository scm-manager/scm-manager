/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

    assertThat(failure.getUrl()).isEqualTo("https://www.scm-manager.org/docs/latest/en/user/repo/health-checks/hyperdrive");
  }

  @Test
  void shouldCreateTemplatedUrlForGivenVersion() {
    HealthCheckFailure failure = new HealthCheckFailure("1", "hyperdrive", urlForTitle("hyperdrive"), "Far too fast");

    assertThat(failure.getUrl("1.17.x")).isEqualTo("https://www.scm-manager.org/docs/1.17.x/en/user/repo/health-checks/hyperdrive");
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
