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
