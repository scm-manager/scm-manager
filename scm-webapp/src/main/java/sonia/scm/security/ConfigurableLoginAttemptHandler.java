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

import com.google.common.base.MoreObjects;
import com.google.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Configurable implementation of {@link LoginAttemptHandler}.
 * 
 * @author Sebastian Sdorra
 * @since 1.34
 */
@Singleton
public class ConfigurableLoginAttemptHandler implements LoginAttemptHandler {
  
  /**
   * the logger for ConfigurableLoginAttemptHandler
   */
  private static final Logger LOG =
    LoggerFactory.getLogger(ConfigurableLoginAttemptHandler.class);

  //~--- fields ---------------------------------------------------------------
  
  private final ConcurrentMap<Object, LoginAttempt> attempts = new ConcurrentHashMap<>();
  
  private final ScmConfiguration configuration;
  
  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs a new instance.
   *
   * @param configuration scm main configuration
   */
  @Inject
  public ConfigurableLoginAttemptHandler(ScmConfiguration configuration) {
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  @Override
  public void beforeAuthentication(AuthenticationToken token) throws AuthenticationException {
    if (isEnabled()) {
      handleBeforeAuthentication(token);
    } else {
      LOG.trace("LoginAttemptHandler is disabled");
    }
  }

  @Override
  public void onSuccessfulAuthentication(AuthenticationToken token, AuthenticationInfo info)
    throws AuthenticationException {
    if (isEnabled()) {
      handleOnSuccessfulAuthentication(token);
    } else {
      LOG.trace("LoginAttemptHandler is disabled");
    }
  }

  @Override
  public void onUnsuccessfulAuthentication(AuthenticationToken token, AuthenticationInfo info) 
    throws AuthenticationException {
    if (isEnabled()) {
      handleOnUnsuccessfulAuthentication(token);
    } else {
      LOG.trace("LoginAttemptHandler is disabled");
    }
  }
  
  private void handleBeforeAuthentication(AuthenticationToken token) {
    LoginAttempt attempt = getAttempt(token);
    long time = System.currentTimeMillis() - attempt.lastAttempt;

    if (time > getLoginAttemptLimitTimeout()) {
      LOG.debug("login attempts {} of {} are timetout", attempt, token.getPrincipal());
      attempt.reset();
    } else if (attempt.counter >= configuration.getLoginAttemptLimit()) {
      LOG.warn("account {} is temporary locked, because of {}", token.getPrincipal(), attempt);
      attempt.increase();

      throw new ExcessiveAttemptsException("account is temporary locked");
    }
  }
  
  private void handleOnSuccessfulAuthentication(AuthenticationToken token) throws AuthenticationException {
    LoginAttempt attempt = getAttempt(token);
    LOG.debug("reset login attempts {} for {}, because of successful login", attempt, token.getPrincipal());
    attempt.reset();
  }

  private void handleOnUnsuccessfulAuthentication(AuthenticationToken token) throws AuthenticationException {
    LoginAttempt attempt = getAttempt(token);
    LOG.debug("increase failed login attempts {} for {}", attempt, token.getPrincipal());
    attempt.increase();
  }

  //~--- get methods ----------------------------------------------------------

  private LoginAttempt getAttempt(AuthenticationToken token){
    LoginAttempt freshAttempt = new LoginAttempt();
    LoginAttempt attempt = attempts.putIfAbsent(token.getPrincipal(), freshAttempt);

    if (attempt == null) {
      attempt = freshAttempt;
    }

    return attempt;
  }

  private long getLoginAttemptLimitTimeout() {
    return TimeUnit.SECONDS.toMillis( configuration.getLoginAttemptLimitTimeout() );
  }

  private boolean isEnabled() {
    return (configuration.getLoginAttemptLimit() > 0) && (configuration.getLoginAttemptLimitTimeout() > 0l);
  }

  //~--- inner classes --------------------------------------------------------

  private static class LoginAttempt {

    private int counter = 0;
    private long lastAttempt = -1l;

    synchronized void increase() {
      counter++;
      lastAttempt = System.currentTimeMillis();
    }

    synchronized void reset() {
      lastAttempt = -1l;
      counter = 0;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
                    .add("counter", counter)
                    .add("lastAttempt", lastAttempt)
                    .toString();
    }
    
  }

}
