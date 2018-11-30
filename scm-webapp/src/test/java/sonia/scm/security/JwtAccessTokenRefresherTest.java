package sonia.scm.security;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import static java.time.Duration.ofHours;
import static java.time.Duration.ofMinutes;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SubjectAware(
  username = "user",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@RunWith(MockitoJUnitRunner.class)
public class JwtAccessTokenRefresherTest {

  private static final Instant NOW = Instant.now().truncatedTo(SECONDS);
  private static final Instant TOKEN_CREATION = NOW.minus(ofMinutes(1));

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private SecureKeyResolver keyResolver;
  @Mock
  private JwtAccessTokenRefreshStrategy refreshStrategy;
  @Mock
  private Clock refreshClock;

  private KeyGenerator keyGenerator = () -> "key";

  private JwtAccessTokenRefresher refresher;
  private JwtAccessTokenBuilder tokenBuilder;

  @Before
  public void initKeyResolver() {
    byte[] bytes = new byte[256];
    new Random().nextBytes(bytes);
    SecureKey secureKey = new SecureKey(bytes, System.currentTimeMillis());
    when(keyResolver.getSecureKey(any())).thenReturn(secureKey);

    Clock creationClock = mock(Clock.class);
    when(creationClock.instant()).thenReturn(TOKEN_CREATION);
    tokenBuilder = new JwtAccessTokenBuilderFactory(keyGenerator, keyResolver, Collections.emptySet(), creationClock).create();

    JwtAccessTokenBuilderFactory refreshBuilderFactory = new JwtAccessTokenBuilderFactory(keyGenerator, keyResolver, Collections.emptySet(), refreshClock);
    refresher = new JwtAccessTokenRefresher(refreshBuilderFactory, refreshStrategy, refreshClock);
    when(refreshClock.instant()).thenReturn(NOW);
    when(refreshStrategy.shouldBeRefreshed(any())).thenReturn(true);

    // set default expiration values
    tokenBuilder
      .expiresIn(5, MINUTES)
      .refreshableFor(10, MINUTES);
  }

  @Test
  public void shouldNotRefreshTokenWithDisabledRefresh() {
    JwtAccessToken oldToken = tokenBuilder
      .refreshableFor(0, MINUTES)
      .build();

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    assertThat(refreshedToken).isEmpty();
  }

  @Test
  public void shouldNotRefreshTokenWhenTokenExpired() {
    Instant afterNormalExpiration = NOW.plus(ofMinutes(6));
    when(refreshClock.instant()).thenReturn(afterNormalExpiration);
    JwtAccessToken oldToken = tokenBuilder.build();

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    assertThat(refreshedToken).isEmpty();
  }

  @Test
  public void shouldNotRefreshTokenWhenRefreshExpired() {
    Instant afterRefreshExpiration = Instant.now().plus(ofMinutes(2));
    when(refreshClock.instant()).thenReturn(afterRefreshExpiration);
    JwtAccessToken oldToken = tokenBuilder
      .refreshableFor(1, MINUTES)
      .build();

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    assertThat(refreshedToken).isEmpty();
  }

  @Test
  public void shouldNotRefreshTokenWhenStrategyDoesNotSaySo() {
    JwtAccessToken oldToken = tokenBuilder.build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(false);

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    assertThat(refreshedToken).isEmpty();
  }

  @Test
  public void shouldRefreshTokenWithParentId() {
    JwtAccessToken oldToken = tokenBuilder.build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(true);

    Optional<JwtAccessToken> refreshedTokenResult = refresher.refresh(oldToken);

    assertThat(refreshedTokenResult).isNotEmpty();
    JwtAccessToken refreshedToken = refreshedTokenResult.get();
    assertThat(refreshedToken.getParentKey()).get().isEqualTo("key");
  }

  @Test
  public void shouldRefreshTokenWithSameExpiration() {
    JwtAccessToken oldToken = tokenBuilder.build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(true);

    Optional<JwtAccessToken> refreshedTokenResult = refresher.refresh(oldToken);

    assertThat(refreshedTokenResult).isNotEmpty();
    JwtAccessToken refreshedToken = refreshedTokenResult.get();
    assertThat(refreshedToken.getExpiration()).isEqualTo(Date.from(NOW.plus(ofMinutes(5))));
  }

  @Test
  public void shouldRefreshTokenWithSameRefreshExpiration() {
    JwtAccessToken oldToken = tokenBuilder.build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(true);

    Optional<JwtAccessToken> refreshedTokenResult = refresher.refresh(oldToken);

    assertThat(refreshedTokenResult).isNotEmpty();
    JwtAccessToken refreshedToken = refreshedTokenResult.get();
    assertThat(refreshedToken.getRefreshExpiration()).get().isEqualTo(Date.from(TOKEN_CREATION.plus(ofMinutes(10))));
  }
}
