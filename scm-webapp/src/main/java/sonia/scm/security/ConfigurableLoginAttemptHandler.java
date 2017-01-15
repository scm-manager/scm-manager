/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.security;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.config.ScmConfiguration;

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
      return Objects.toStringHelper(this)
                    .add("counter", counter)
                    .add("lastAttempt", lastAttempt)
                    .toString();
    }
    
  }

}
