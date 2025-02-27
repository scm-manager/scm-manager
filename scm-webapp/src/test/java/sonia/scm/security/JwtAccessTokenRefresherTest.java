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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.user.User;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static java.time.Duration.ofMinutes;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.security.SecureKeyTestUtil.createSecureKey;

@ExtendWith(MockitoExtension.class)
class JwtAccessTokenRefresherTest {

  private static final Instant NOW = Instant.now().truncatedTo(SECONDS);
  private static final Instant TOKEN_CREATION = NOW.minus(ofMinutes(1));

  @Mock
  private SecureKeyResolver keyResolver;
  @Mock
  private JwtConfig jwtConfig;

  private ScmConfiguration scmConfiguration;
  @Mock
  private JwtAccessTokenRefreshStrategy refreshStrategy;
  @Mock
  private Clock refreshClock;
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private Subject subject;

  private final KeyGenerator keyGenerator = () -> "key";

  private JwtAccessTokenRefresher refresher;
  private JwtAccessTokenBuilder tokenBuilder;

  @BeforeEach
  void initKeyResolver() {
    when(keyResolver.getSecureKey(any())).thenReturn(createSecureKey());

    Clock creationClock = mock(Clock.class);
    when(creationClock.instant()).thenReturn(TOKEN_CREATION);
    tokenBuilder = new JwtAccessTokenBuilderFactory(keyGenerator, keyResolver, jwtConfig, Collections.emptySet(), creationClock, scmConfiguration).create();

    JwtAccessTokenBuilderFactory refreshBuilderFactory = new JwtAccessTokenBuilderFactory(
      keyGenerator,
      keyResolver,
      jwtConfig,
      Collections.emptySet(),
      refreshClock,
      scmConfiguration
    );
    refresher = new JwtAccessTokenRefresher(refreshBuilderFactory, refreshStrategy, refreshClock);
    when(refreshClock.instant()).thenReturn(NOW);
    lenient().when(refreshStrategy.shouldBeRefreshed(any())).thenReturn(true);

    // set default expiration values
    tokenBuilder
      .expiresIn(5, MINUTES)
      .refreshableFor(10, MINUTES);
  }

  @BeforeEach
  void initSubject() {
    ThreadContext.bind(subject);
    when(subject.getPrincipals().oneByType(Scope.class)).thenReturn(Scope.valueOf("trillian"));
    when(subject.getPrincipal()).thenReturn(new User("trillian"));
  }

  @BeforeEach
  void initConfig() {
    this.scmConfiguration = new ScmConfiguration();
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldNotRefreshTokenWithDisabledRefresh() {
    JwtAccessToken oldToken = tokenBuilder
      .refreshableFor(0, MINUTES)
      .build();

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    assertThat(refreshedToken).isEmpty();
  }

  @Test
  void shouldNotRefreshTokenWhenTokenExpired() {
    Instant afterNormalExpiration = NOW.plus(ofMinutes(6));
    when(refreshClock.instant()).thenReturn(afterNormalExpiration);
    JwtAccessToken oldToken = tokenBuilder.build();

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    assertThat(refreshedToken).isEmpty();
  }

  @Test
  void shouldNotRefreshTokenWhenRefreshExpired() {
    Instant afterRefreshExpiration = Instant.now().plus(ofMinutes(2));
    when(refreshClock.instant()).thenReturn(afterRefreshExpiration);
    JwtAccessToken oldToken = tokenBuilder
      .refreshableFor(1, MINUTES)
      .build();

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    assertThat(refreshedToken).isEmpty();
  }

  @Test
  void shouldNotRefreshTokenWhenStrategyDoesNotSaySo() {
    JwtAccessToken oldToken = tokenBuilder.build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(false);

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    assertThat(refreshedToken).isEmpty();
  }

  @Test
  void shouldRefreshTokenWithParentId() {
    JwtAccessToken oldToken = tokenBuilder.build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(true);

    Optional<JwtAccessToken> refreshedTokenResult = refresher.refresh(oldToken);

    assertThat(refreshedTokenResult).isNotEmpty();
    JwtAccessToken refreshedToken = refreshedTokenResult.get();
    assertThat(refreshedToken.getParentKey()).get().isEqualTo("key");
  }

  @Test
  void shouldRefreshTokenWithSameExpiration() {
    JwtAccessToken oldToken = tokenBuilder.build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(true);

    Optional<JwtAccessToken> refreshedTokenResult = refresher.refresh(oldToken);

    assertThat(refreshedTokenResult).isNotEmpty();
    JwtAccessToken refreshedToken = refreshedTokenResult.get();
    assertThat(refreshedToken.getExpiration()).isEqualTo(Date.from(NOW.plus(ofMinutes(5))));
  }

  @Test
  void shouldRefreshTokenWithSameRefreshExpiration() {
    JwtAccessToken oldToken = tokenBuilder.build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(true);

    Optional<JwtAccessToken> refreshedTokenResult = refresher.refresh(oldToken);

    assertThat(refreshedTokenResult).isNotEmpty();
    JwtAccessToken refreshedToken = refreshedTokenResult.get();
    assertThat(refreshedToken.getRefreshExpiration()).get().isEqualTo(Date.from(TOKEN_CREATION.plus(ofMinutes(10))));
  }

  @Test
  void shouldNotRefreshTokenWhenPrincipalIsMissing() {
    JwtAccessToken oldToken = tokenBuilder.build();

    when(subject.getPrincipals()).thenReturn(null);

    Optional<JwtAccessToken> refreshedTokenResult = refresher.refresh(oldToken);

    assertThat(refreshedTokenResult).isEmpty();
  }

}
