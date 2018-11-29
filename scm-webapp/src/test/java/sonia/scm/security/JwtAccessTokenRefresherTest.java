package sonia.scm.security;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SubjectAware(
  username = "user",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@RunWith(MockitoJUnitRunner.class)
public class JwtAccessTokenRefresherTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  @Mock
  private SecureKeyResolver keyResolver;
  @Mock
  private JwtAccessTokenRefreshStrategy refreshStrategy;
  private JwtAccessTokenBuilderFactory builderFactory;
  private JwtAccessTokenRefresher refresher;
  private JwtAccessTokenBuilder tokenBuilder;

  @Before
  public void initKeyResolver() {
    byte[] bytes = new byte[256];
    new Random().nextBytes(bytes);
    SecureKey secureKey = new SecureKey(bytes, System.currentTimeMillis());
    when(keyResolver.getSecureKey(any())).thenReturn(secureKey);

    builderFactory = new JwtAccessTokenBuilderFactory(new DefaultKeyGenerator(), keyResolver, Collections.emptySet());
    refresher = new JwtAccessTokenRefresher(builderFactory, refreshStrategy);
    tokenBuilder = builderFactory.create();
  }

  @Test
  public void shouldNotRefreshTokenWithDisabledRefresh() {
    JwtAccessToken oldToken = tokenBuilder
      .refreshableFor(0, TimeUnit.MINUTES)
      .build();

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    Assertions.assertThat(refreshedToken).isEmpty();
  }

  @Test
  public void shouldNotRefreshTokenWhenStrategyDoesNotSaySo() {
    JwtAccessToken oldToken = tokenBuilder
      .refreshableFor(10, TimeUnit.MINUTES)
      .build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(false);

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    Assertions.assertThat(refreshedToken).isEmpty();
  }

  @Test
  public void shouldRefreshTokenWithEnabledRefresh() {
    JwtAccessToken oldToken = tokenBuilder
      .refreshableFor(1, TimeUnit.MINUTES)
      .build();
    when(refreshStrategy.shouldBeRefreshed(oldToken)).thenReturn(true);

    Optional<JwtAccessToken> refreshedToken = refresher.refresh(oldToken);

    Assertions.assertThat(refreshedToken).isNotEmpty();
  }
}
