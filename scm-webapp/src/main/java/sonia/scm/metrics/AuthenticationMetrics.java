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

package sonia.scm.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class AuthenticationMetrics {

  private AuthenticationMetrics() {}

  /**
   * Creates counter to track amount of login attempts to SCM-Manager.
   *
   * @param registry meter registry
   * @param type type of login e.g.: api_key, bearer_token, etc.
   * @return new {@link Counter}
   */
  public static Counter loginAttempts(MeterRegistry registry, String type) {
    return Counter
      .builder("scm.auth.login.attempts")
      .description("The amount of login attempts to SCM-Manager")
      .tags("type", type)
      .register(registry);
  }

  /**
   * Creates counter to track amount of failed logins to SCM-Manager.
   *
   * @param registry meter registry
   * @param type type of failed login, e.g.: UI/REST
   * @return new {@link Counter}
   */
  public static Counter loginFailed(MeterRegistry registry, String type) {
    return Counter
      .builder("scm.auth.login.failed")
      .tags("type", type)
      .description("The amount of failed logins to SCM-Manager")
      .register(registry);
  }

  /**
   * Creates counter to track amount of successful accesses to SCM-Manager.
   *
   * @param registry meter registry
   * @param type type of access e.g.: api_key, bearer_token, etc.
   * @return new {@link Counter}
   */
  public static Counter accessSuccessful(MeterRegistry registry, String type) {
    return Counter
      .builder("scm.auth.access.successful")
      .description("The amount of successful logins to SCM-Manager")
      .tags("type", type)
      .register(registry);
  }

  /**
   * Creates counter to track amount of failed accesses to SCM-Manager.
   *
   * @param registry meter registry
   * @return new {@link Counter}
   */
  public static Counter accessFailed(MeterRegistry registry) {
    return Counter
      .builder("scm.auth.access.failed")
      .description("The amount of failed accesses to SCM-Manager")
      .register(registry);
  }

}
