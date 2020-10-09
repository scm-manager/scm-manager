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

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.MergableAuthenticationInfo;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmAtLeastOneSuccessfulStrategyTest {

  @Mock
  private Realm realm;

  @Mock
  private AuthenticationToken token;

  @Mock
  MergableAuthenticationInfo singleRealmInfo;

  @Mock
  MergableAuthenticationInfo aggregateInfo;

  @Mock
  TokenExpiredException tokenExpiredException;

  @Mock
  TokenValidationFailedException tokenValidationFailedException;

  @Mock
  AuthenticationException authenticationException;

  @Mock
  PrincipalCollection principalCollection;

  @Test
  public void shouldAddNonNullThrowableToList() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy();
    strategy.threadLocal.set(new ArrayList<>());

    strategy.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, tokenExpiredException);

    assertThat(strategy.threadLocal.get()).hasSize(1);
    assertThat(strategy.threadLocal.get().get(0)).isEqualTo(tokenExpiredException);
  }

  @Test(expected = TokenExpiredException.class)
  public void shouldRethrowTokenExpiredException() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy();
    strategy.threadLocal.set(singletonList(tokenExpiredException));

    strategy.afterAllAttempts(token, aggregateInfo);
  }

  @Test(expected = TokenValidationFailedException.class)
  public void shouldRethrowTokenValidationFailedException() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy();
    strategy.threadLocal.set(singletonList(tokenValidationFailedException));

    strategy.afterAllAttempts(token, aggregateInfo);
  }

  @Test(expected = TokenExpiredException.class)
  public void shouldPrioritizeRethrowingTokenExpiredExceptionOverTokenValidationFailedException() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy();
    strategy.threadLocal.set(Arrays.asList(tokenValidationFailedException, tokenExpiredException));

    strategy.afterAllAttempts(token, aggregateInfo);
  }

  @Test(expected = AuthenticationException.class)
  public void shouldThrowGenericErrorIfNonTokenExpiredExceptionWasCaught() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy();
    strategy.threadLocal.set(singletonList(authenticationException));

    strategy.afterAllAttempts(token, aggregateInfo);
  }

  @Test()
  public void shouldNotRethrowExceptionIfAuthenticationSuccessful() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy();
    strategy.threadLocal.set(singletonList(tokenExpiredException));
    when(aggregateInfo.getPrincipals()).thenReturn(principalCollection);
    when(principalCollection.isEmpty()).thenReturn(false);

    final AuthenticationInfo authenticationInfo = strategy.afterAllAttempts(token, aggregateInfo);

    assertThat(authenticationInfo).isNotNull();
  }

}
