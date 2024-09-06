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

package sonia.scm.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

public class AuthenticationMetrics {

  private AuthenticationMetrics() {
  }

  /**
   * Creates counter to track amount of login attempts to SCM-Manager.
   *
   * @param registry meter registry
   * @param type     type of login e.g.: api_key, bearer_token, etc.
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
   * @param type     type of failed login, e.g.: UI/REST
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
   * Creates counter to track amount of logouts to SCM-Manager.
   *
   * @param registry meter registry
   * @param type     type of logout, e.g.: UI/REST
   * @return new {@link Counter}
   */
  public static Counter logout(MeterRegistry registry, String type) {
    return Counter
      .builder("scm.auth.logout")
      .description("The amount of logouts from SCM-Manager")
      .tags("type", type)
      .register(registry);
  }

  /**
   * Creates counter to track amount of token refreshes by SCM-Manager.
   *
   * @param registry meter registry
   * @param type     type of refreshed token, e.g.: JWT
   * @return new {@link Counter}
   */
  public static Counter tokenRefresh(MeterRegistry registry, String type) {
    return Counter
      .builder("scm.auth.token.refresh")
      .description("The amount of authentication token refreshes")
      .tags("type", type)
      .register(registry);
  }

  /**
   * Creates counter to track amount of successful accesses to SCM-Manager realms with token.
   *
   * @param registry meter registry
   * @param realm    type of realm e.g.: {@link sonia.scm.security.BearerRealm}
   * @param token    type of token e.g.: {@link org.apache.shiro.authc.UsernamePasswordToken},
   * @return new {@link Counter}
   */
  public static Counter accessRealmSuccessful(MeterRegistry registry, String realm, String token) {
    return Counter
      .builder("scm.auth.realm.successful")
      .description("The amount of successful login to the realm")
      .tags("realm", realm, "token", token)
      .register(registry);
  }

  /**
   * Creates counter to track amount of successful accesses to SCM-Manager.
   *
   * @param registry meter registry
   * @return new {@link Counter}
   */
  public static Counter accessSuccessful(MeterRegistry registry, String tokenType) {
    return Counter
      .builder("scm.auth.access.successful")
      .description("The amount of successful accesses to SCM-Manager")
      .tags("token", tokenType)
      .register(registry);
  }

  /**
   * Creates counter to track amount of failed accesses to SCM-Manager.
   *
   * @param registry meter registry
   * @return new {@link Counter}
   */
  public static Counter accessFailed(MeterRegistry registry, String tokenType) {
    return Counter
      .builder("scm.auth.access.failed")
      .description("The amount of failed accesses to SCM-Manager")
      .tags("token", tokenType)
      .register(registry);
  }

}
