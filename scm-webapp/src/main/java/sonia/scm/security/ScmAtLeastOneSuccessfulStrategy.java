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

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.inject.Inject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.pam.AbstractAuthenticationStrategy;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import sonia.scm.metrics.AuthenticationMetrics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class ScmAtLeastOneSuccessfulStrategy extends AbstractAuthenticationStrategy {

  final ThreadLocal<List<Throwable>> threadLocal = new ThreadLocal<>();

  private final MeterRegistry meterRegistry;

  @Inject
  public ScmAtLeastOneSuccessfulStrategy(MeterRegistry meterRegistry) {
    this.meterRegistry = meterRegistry;
  }

  @Override
  public AuthenticationInfo beforeAllAttempts(Collection<? extends Realm> realms, AuthenticationToken token) throws AuthenticationException {
    this.threadLocal.set(new ArrayList<>());
    return super.beforeAllAttempts(realms, token);
  }

  @Override
  public AuthenticationInfo afterAttempt(Realm realm, AuthenticationToken token, AuthenticationInfo singleRealmInfo, AuthenticationInfo aggregateInfo, Throwable t) throws AuthenticationException {
    if (t != null) {
      this.threadLocal.get().add(t);
    }

    if (isAuthenticationSuccessful(singleRealmInfo)) {
      AuthenticationMetrics.accessRealmSuccessful(meterRegistry, realm.getClass().getName(), token.getClass().getName()).increment();
    }

    return super.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, t);
  }

  @Override
  public AuthenticationInfo afterAllAttempts(AuthenticationToken token, AuthenticationInfo aggregate) {
    final List<Throwable> throwables = threadLocal.get();
    threadLocal.remove();

    String tokenType = token.getClass().getName();

    if (isAuthenticationSuccessful(aggregate)) {
      AuthenticationMetrics.accessSuccessful(meterRegistry, tokenType).increment();
      return aggregate;
    }
    AuthenticationMetrics.accessFailed(meterRegistry, tokenType).increment();

    Optional<? extends AuthenticationException> specializedException = findSpecializedException(throwables);

    if (specializedException.isPresent()) {
      throw specializedException.get();
    } else {
      throw createAuthenticationException(token);
    }
  }

  private static boolean isAuthenticationSuccessful(AuthenticationInfo aggregate) {
    return aggregate != null && isNotEmpty(aggregate.getPrincipals());
  }

  private static boolean isNotEmpty(PrincipalCollection pc) {
    return pc != null && !pc.isEmpty();
  }

  private static Optional<TokenExpiredException> findTokenExpiredException(List<Throwable> throwables) {
    return throwables.stream().filter(t -> t instanceof TokenExpiredException).findFirst().map(t -> (TokenExpiredException) t);
  }

  private static Optional<AuthenticationException> findTokenValidationFailedException(List<Throwable> throwables) {
    return throwables.stream().filter(t -> t instanceof TokenValidationFailedException).findFirst().map(t -> (TokenValidationFailedException) t);
  }

  private static Optional<? extends AuthenticationException> findSpecializedException(List<Throwable> throwables) {
    Optional<TokenExpiredException> tokenExpiredException = findTokenExpiredException(throwables);
    if (tokenExpiredException.isPresent()) {
      return tokenExpiredException;
    }
    return findTokenValidationFailedException(throwables);
  }

  private static AuthenticationException createAuthenticationException(AuthenticationToken token) {
    return new AuthenticationException("Authentication token of type [" + token.getClass() + "] " +
      "could not be authenticated by any configured realms.  Please ensure that at least one realm can " +
      "authenticate these tokens.");
  }

}
