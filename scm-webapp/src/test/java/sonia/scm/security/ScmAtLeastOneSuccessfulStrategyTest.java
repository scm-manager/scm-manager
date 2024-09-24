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

import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.MergableAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScmAtLeastOneSuccessfulStrategyTest {

  @Mock
  private Realm realm;

  @Mock
  private UsernamePasswordToken token;

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
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy(new SimpleMeterRegistry());
    strategy.threadLocal.set(new ArrayList<>());

    strategy.afterAttempt(realm, token, singleRealmInfo, aggregateInfo, tokenExpiredException);

    assertThat(strategy.threadLocal.get()).hasSize(1);
    assertThat(strategy.threadLocal.get().get(0)).isEqualTo(tokenExpiredException);
  }

  @Test(expected = TokenExpiredException.class)
  public void shouldRethrowTokenExpiredException() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy(new SimpleMeterRegistry());
    strategy.threadLocal.set(singletonList(tokenExpiredException));

    strategy.afterAllAttempts(token, aggregateInfo);
  }

  @Test(expected = TokenValidationFailedException.class)
  public void shouldRethrowTokenValidationFailedException() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy(new SimpleMeterRegistry());
    strategy.threadLocal.set(singletonList(tokenValidationFailedException));

    strategy.afterAllAttempts(token, aggregateInfo);
  }

  @Test(expected = TokenExpiredException.class)
  public void shouldPrioritizeRethrowingTokenExpiredExceptionOverTokenValidationFailedException() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy(new SimpleMeterRegistry());
    strategy.threadLocal.set(Arrays.asList(tokenValidationFailedException, tokenExpiredException));

    strategy.afterAllAttempts(token, aggregateInfo);
  }

  @Test(expected = AuthenticationException.class)
  public void shouldThrowGenericErrorIfNonTokenExpiredExceptionWasCaught() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy(new SimpleMeterRegistry());
    strategy.threadLocal.set(singletonList(authenticationException));

    strategy.afterAllAttempts(token, aggregateInfo);
  }

  @Test()
  public void shouldNotRethrowExceptionIfAuthenticationSuccessful() {
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy(new SimpleMeterRegistry());
    strategy.threadLocal.set(singletonList(tokenExpiredException));
    when(aggregateInfo.getPrincipals()).thenReturn(principalCollection);
    when(principalCollection.isEmpty()).thenReturn(false);

    final AuthenticationInfo authenticationInfo = strategy.afterAllAttempts(token, aggregateInfo);

    assertThat(authenticationInfo).isNotNull();
  }

  @Test()
  public void shouldTrackSuccessfulRealmAuthenticationMetrics() {
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy(meterRegistry);
    strategy.threadLocal.set(singletonList(tokenExpiredException));
    when(aggregateInfo.getPrincipals()).thenReturn(principalCollection);
    when(principalCollection.isEmpty()).thenReturn(false);

    DefaultRealm realm = mock(DefaultRealm.class);

    strategy.afterAttempt(realm, token, aggregateInfo, null, null);

    assertThat(meterRegistry.getMeters()).hasSize(1);
    Optional<Meter> realmAccessMeter = meterRegistry.getMeters()
      .stream()
      .filter(m -> m.getId().getName().equals("scm.auth.realm.successful"))
      .findFirst();
    assertThat(realmAccessMeter).isPresent();
    assertThat(realmAccessMeter.get().measure().iterator().next().getValue()).isEqualTo(1);
    assertThat(realmAccessMeter.get().getId().getTags()).contains(
      Tag.of("realm", "sonia.scm.security.DefaultRealm"),
      Tag.of("token", "org.apache.shiro.authc.UsernamePasswordToken")
    );
  }

  @Test()
  public void shouldTrackGeneralSuccessfulAuthenticationMetrics() {
    MeterRegistry meterRegistry = new SimpleMeterRegistry();
    final ScmAtLeastOneSuccessfulStrategy strategy = new ScmAtLeastOneSuccessfulStrategy(meterRegistry);
    strategy.threadLocal.set(singletonList(tokenExpiredException));
    when(aggregateInfo.getPrincipals()).thenReturn(principalCollection);
    when(principalCollection.isEmpty()).thenReturn(false);

    strategy.afterAllAttempts(token, aggregateInfo);

    assertThat(meterRegistry.getMeters()).hasSize(1);
    Optional<Meter> accessMeter = meterRegistry.getMeters()
      .stream()
      .filter(m -> m.getId().getName().equals("scm.auth.access.successful"))
      .findFirst();
    assertThat(accessMeter).isPresent();
    assertThat(accessMeter.get().measure().iterator().next().getValue()).isEqualTo(1);
    assertThat(accessMeter.get().getId().getTags()).contains(
      Tag.of("token", "org.apache.shiro.authc.UsernamePasswordToken")
    );
  }
}
