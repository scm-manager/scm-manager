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
 * @since 1.34
 */
@Singleton
public class ConfigurableLoginAttemptHandler implements LoginAttemptHandler {
  
 
  private static final Logger LOG =
    LoggerFactory.getLogger(ConfigurableLoginAttemptHandler.class);


  private final ConcurrentMap<Object, LoginAttempt> attempts = new ConcurrentHashMap<>();
  
  private final ScmConfiguration configuration;
  

  /**
   * Constructs a new instance.
   *
   * @param configuration scm main configuration
   */
  @Inject
  public ConfigurableLoginAttemptHandler(ScmConfiguration configuration) {
    this.configuration = configuration;
  }


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
