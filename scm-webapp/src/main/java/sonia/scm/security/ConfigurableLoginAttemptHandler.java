/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.ExcessiveAttemptsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.config.ScmConfiguration;
import sonia.scm.web.security.AuthenticationResult;

//~--- JDK imports ------------------------------------------------------------

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ConfigurableLoginAttemptHandler implements LoginAttemptHandler
{

  /**
   * the logger for ConfigurableLoginAttemptHandler
   */
  private static final Logger logger =
    LoggerFactory.getLogger(ConfigurableLoginAttemptHandler.class);

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param configuration
   */
  @Inject
  public ConfigurableLoginAttemptHandler(ScmConfiguration configuration)
  {
    this.configuration = configuration;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param token
   *
   * @throws AuthenticationException
   */
  @Override
  public void beforeAuthentication(AuthenticationToken token)
    throws AuthenticationException
  {
    if (isEnabled())
    {
      handleBeforeAuthentication(token);
    }
    else
    {
      logger.trace("LoginAttemptHandler is disabled");
    }
  }

  /**
   * Method description
   *
   *
   * @param token
   * @param result
   *
   * @throws AuthenticationException
   */
  @Override
  public void onSuccessfulAuthentication(AuthenticationToken token,
    AuthenticationResult result)
    throws AuthenticationException
  {
    if (isEnabled())
    {
      handleOnSuccessfulAuthentication(token);
    }
    else
    {
      logger.trace("LoginAttemptHandler is disabled");
    }
  }

  /**
   * Method description
   *
   *
   * @param token
   * @param result
   *
   * @throws AuthenticationException
   */
  @Override
  public void onUnsuccessfulAuthentication(AuthenticationToken token,
    AuthenticationResult result)
    throws AuthenticationException
  {
    if (isEnabled())
    {
      handleOnUnsuccessfulAuthentication(token);
    }
    else
    {
      logger.trace("LoginAttemptHandler is disabled");
    }
  }

  /**
   * Method description
   *
   *
   * @param token
   */
  private void handleBeforeAuthentication(AuthenticationToken token)
  {
    LoginAttempt attempt = getAttempt(token);
    long time = System.currentTimeMillis() - attempt.lastAttempt;

    if (time > getLoginAttemptLimitTimeout())
    {
      logger.debug("reset login attempts for {}, because of time",
        token.getPrincipal());
      attempt.reset();
    }
    else if (attempt.counter >= configuration.getLoginAttemptLimit())
    {
      logger.warn("account {} is temporary locked, because of {}",
        token.getPrincipal(), attempt);
      attempt.increase();

      throw new ExcessiveAttemptsException("account is temporary locked");
    }
  }

  /**
   * Method description
   *
   *
   * @param token
   * @param result
   *
   * @throws AuthenticationException
   */
  private void handleOnSuccessfulAuthentication(AuthenticationToken token)
    throws AuthenticationException
  {
    logger.debug("reset login attempts for {}, because of successful login",
      token.getPrincipal());
    getAttempt(token).reset();
  }

  /**
   * Method description
   *
   *
   * @param token
   * @param result
   *
   * @throws AuthenticationException
   */
  private void handleOnUnsuccessfulAuthentication(AuthenticationToken token)
    throws AuthenticationException
  {
    logger.debug("increase failed login attempts for {}", token.getPrincipal());

    LoginAttempt attempt = getAttempt(token);

    attempt.increase();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param token
   *
   * @return
   */
  private LoginAttempt getAttempt(AuthenticationToken token)
  {
    LoginAttempt freshAttempt = new LoginAttempt();
    LoginAttempt attempt = attempts.putIfAbsent(token.getPrincipal(),
                             freshAttempt);

    if (attempt == null)
    {
      attempt = freshAttempt;
    }

    return attempt;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private long getLoginAttemptLimitTimeout()
  {
    return TimeUnit.SECONDS.toMillis(
      configuration.getLoginAttemptLimitTimeout());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private boolean isEnabled()
  {
    return (configuration.getLoginAttemptLimit() > 0)
      && (configuration.getLoginAttemptLimitTimeout() > 0l);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Login attempt
   */
  private static class LoginAttempt
  {

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String toString()
    {
      //J-
      return Objects.toStringHelper(this)
                    .add("counter", counter)
                    .add("lastAttempt", lastAttempt)
                    .toString();
      //J+
    }

    /**
     * Method description
     *
     */
    synchronized void increase()
    {
      counter++;
      lastAttempt = System.currentTimeMillis();
    }

    /**
     * Method description
     *
     */
    synchronized void reset()
    {
      lastAttempt = -1l;
      counter = 0;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private int counter = 0;

    /** Field description */
    private long lastAttempt = -1l;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ConcurrentMap<Object, LoginAttempt> attempts =
    new ConcurrentHashMap<Object, LoginAttempt>();

  /** Field description */
  private final ScmConfiguration configuration;
}
