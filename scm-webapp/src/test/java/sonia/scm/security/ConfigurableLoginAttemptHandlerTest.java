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

package sonia.scm.security;

import java.util.concurrent.TimeUnit;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.junit.Test;
import sonia.scm.config.ScmConfiguration;

/**
 * Unit tests for {@link ConfigurableLoginAttemptHandler}.
 * 
 */
public class ConfigurableLoginAttemptHandlerTest {

  /**
   * Tests login attempt limit reached.
   */
  @Test(expected = ExcessiveAttemptsException.class)
  public void testLoginAttemptLimitReached() {
    LoginAttemptHandler handler = createHandler(2, 2);
    UsernamePasswordToken token = new UsernamePasswordToken("hansolo", "hobbo");

    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, new SimpleAuthenticationInfo());
    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, new SimpleAuthenticationInfo());
    handler.beforeAuthentication(token);
  }

  /**
   * Tests login attempts limit timeout.
   *
   * @throws InterruptedException
   */
  @Test
  public void testLoginAttemptLimitTimeout() throws InterruptedException {
    LoginAttemptHandler handler = createHandler(2, 1);
    UsernamePasswordToken token = new UsernamePasswordToken("hansolo", "hobbo");

    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, new SimpleAuthenticationInfo());
    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, new SimpleAuthenticationInfo());
    Thread.sleep(TimeUnit.MILLISECONDS.toMillis(1200L));
    handler.beforeAuthentication(token);
  }

  /**
   * Tests login attempt limit reset on success
   *
   *
   * @throws InterruptedException
   */
  @Test
  public void testLoginAttemptResetOnSuccess() throws InterruptedException {
    LoginAttemptHandler handler = createHandler(2, 1);
    UsernamePasswordToken token = new UsernamePasswordToken("hansolo", "hobbo");

    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, new SimpleAuthenticationInfo());
    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, new SimpleAuthenticationInfo());

    handler.onSuccessfulAuthentication(token, new SimpleAuthenticationInfo());

    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, new SimpleAuthenticationInfo());
    handler.beforeAuthentication(token);
    handler.onUnsuccessfulAuthentication(token, new SimpleAuthenticationInfo());
  }

  private LoginAttemptHandler createHandler(int loginAttemptLimit, long loginAttemptLimitTimeout) {
    ScmConfiguration configuration = new ScmConfiguration();

    configuration.setLoginAttemptLimit(loginAttemptLimit);
    configuration.setLoginAttemptLimitTimeout(loginAttemptLimitTimeout);

    return new ConfigurableLoginAttemptHandler(configuration);
  }

}
