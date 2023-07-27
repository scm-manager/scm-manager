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

import com.google.common.collect.Sets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.*;
import static sonia.scm.security.SecureKeyTestUtil.createSecureKey;

/**
 * Unit test for {@link JwtAccessTokenBuilder}.
 *
 * @author Sebastian Sdorra
 */
@ExtendWith(MockitoExtension.class)
class JwtAccessTokenBuilderTest {

  @Mock
  private KeyGenerator keyGenerator;

  @Mock
  private SecureKeyResolver secureKeyResolver;

  private Set<AccessTokenEnricher> enrichers;

  private JwtAccessTokenBuilderFactory factory;

  @Mock
  private Subject subject;
  @Mock
  private PrincipalCollection principalCollection;

  @BeforeEach
  void bindSubject() {
    lenient().when(subject.getPrincipal()).thenReturn("trillian");
    lenient().when(subject.getPrincipals()).thenReturn(principalCollection);
    ThreadContext.bind(subject);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @BeforeEach
  void setUpDependencies() {
    lenient().when(keyGenerator.createKey()).thenReturn("42");
    lenient().when(secureKeyResolver.getSecureKey(anyString())).thenReturn(createSecureKey());
    enrichers = Sets.newHashSet();
    factory = new JwtAccessTokenBuilderFactory(keyGenerator, secureKeyResolver, enrichers);
  }

  @BeforeEach
  void clearSystemProperties() {
    System.clearProperty(JwtSystemProperties.ENDLESS_JWT);
  }

  @Nested
  class SimpleTests {

    /**
     * Prepare mocks and set up object under test.
     */
    @BeforeEach
    void setUpObjectUnderTest() {
      factory = new JwtAccessTokenBuilderFactory(keyGenerator, secureKeyResolver, enrichers);
    }

    /**
     * Tests {@link JwtAccessTokenBuilder#build()}.
     */
    @Test
    void testBuild() {
      JwtAccessToken token = factory.create().subject("dent")
        .issuer("https://www.scm-manager.org")
        .expiresIn(1, TimeUnit.MINUTES)
        .custom("a", "b")
        .scope(Scope.valueOf("repo:*"))
        .build();

      // assert claims
      assertClaims(token);

      // reparse and assert again
      String compact = token.compact();
      assertThat(compact).isNotEmpty();
      Claims claims = Jwts.parser()
        .setSigningKey(secureKeyResolver.getSecureKey("dent").getBytes())
        .parseClaimsJws(compact)
        .getBody();
      assertClaims(new JwtAccessToken(claims, compact));
    }

    private void assertClaims(JwtAccessToken token) {
      assertThat(token.getId()).isNotEmpty();
      assertThat(token.getIssuedAt()).isNotNull();
      assertThat(token.getExpiration()).isNotNull();
      assertThat(token.getExpiration().getTime() > token.getIssuedAt().getTime()).isTrue();
      assertThat(token.getSubject()).isEqualTo("dent");
      assertThat(token.getIssuer()).isNotEmpty();
      assertThat(token.getIssuer()).get().isEqualTo("https://www.scm-manager.org");
      assertThat(token.getCustom("a")).get().isEqualTo("b");
      assertThat(token.getScope()).hasToString("[\"repo:*\"]");
    }

  }

  @Nested
  class ClockTests {

    @Mock
    private Clock clock;

    @BeforeEach
    void setUpObjectUnderTest() {
      factory = new JwtAccessTokenBuilderFactory(keyGenerator, secureKeyResolver, enrichers, clock);
    }

    @Test
    void shouldSetRefreshExpiration() {
      Instant now = Instant.now();
      when(clock.instant()).thenReturn(now);

      JwtAccessToken token = factory.create()
        .subject("dent")
        .refreshableFor(2, TimeUnit.SECONDS)
        .build();

      assertThat(token.getRefreshExpiration()).isPresent();
      Date date = token.getRefreshExpiration().get();

      assertThat(date).hasSameTimeAs(Date.from(now.plusSeconds(2L)));
    }

    @Test
    void shouldSetDefaultRefreshExpiration() {
      Instant now = Instant.now();
      when(clock.instant()).thenReturn(now);

      JwtAccessToken token = factory.create()
        .subject("dent")
        .build();

      assertThat(token.getRefreshExpiration()).isPresent();
      Date date = token.getRefreshExpiration().get();

      long defaultRefresh = JwtAccessTokenBuilder.DEFAULT_REFRESHABLE_UNIT.toMillis(JwtAccessTokenBuilder.DEFAULT_REFRESHABLE);
      assertThat(date).hasSameTimeAs(Date.from(now.plusMillis(defaultRefresh)));
    }

  }

  @Nested
  class FromApiKeyRealm {

    private Scope scope;

    @BeforeEach
    void mockApiKeyRealm() {
      scope = Scope.valueOf("dummy:scope:*");
      lenient().when(principalCollection.getRealmNames()).thenReturn(singleton("ApiTokenRealm"));
      lenient().when(principalCollection.oneByType(Scope.class)).thenReturn(scope);
    }

    @Test
    void shouldCreateJwtAndUsePreviousScope() {
      JwtAccessTokenBuilder builder = factory.create().subject("dent");
      final JwtAccessToken accessToken = builder.build();
      assertThat(accessToken).isNotNull();
      assertThat(accessToken.getSubject()).isEqualTo("dent");
      assertThat((Collection<String>) accessToken.getCustom("scope").get()).containsExactly("dummy:scope:*");
    }

    @Test
    void shouldThrowExceptionWhenScopeAlreadyDefinedInBuilder() {
      when(subject.isPermitted("an:incompatible:scope")).thenReturn(false);
      JwtAccessTokenBuilder builder = factory.create().scope(Scope.valueOf("an:incompatible:scope")).subject("dent");
      assertThrows(AuthorizationException.class, builder::build);
    }

    @Test
    void shouldAcceptRequestedScopeIfPermittedByCurrentScope() {
      when(subject.isPermitted("dummy:scope:42")).thenReturn(true);
      JwtAccessTokenBuilder builder = factory.create().scope(Scope.valueOf("dummy:scope:42")).subject("dent");
      builder.build();
    }
  }

  @Nested
  class FromDefaultRealm {

    @BeforeEach
    void mockDefaultRealm() {
      lenient().when(principalCollection.getRealmNames()).thenReturn(singleton("DefaultRealm"));
    }

    /**
     * Tests {@link JwtAccessTokenBuilder#build()} with subject from shiro context.
     */
    @Test
    void testBuildWithoutSubject() {
      JwtAccessToken token = factory.create().build();
      assertThat(token.getSubject()).isEqualTo("trillian");
    }

    /**
     * Tests {@link JwtAccessTokenBuilder#build()} with explicit subject.
     */
    @Test
    void testBuildWithSubject() {
      JwtAccessToken token = factory.create().subject("dent").build();
      assertThat(token.getSubject()).isEqualTo("dent");
    }

    /**
     * Tests {@link JwtAccessTokenBuilder#build()} with enricher.
     */
    @Test
    void testBuildWithEnricher() {
      enrichers.add((b) -> b.custom("c", "d"));
      JwtAccessToken token = factory.create().subject("dent").build();
      assertThat(token.getCustom("c")).get().isEqualTo("d");
    }


  }

  @Nested
  class WithEndlessJwtFeature {

    @Test
    void testBuildWithEndlessJwtEnabled() {
      System.setProperty(JwtSystemProperties.ENDLESS_JWT, "true");

      JwtAccessToken token = factory.create().subject("Red").issuer("https://scm-manager.org").build();

      assertThat(token.getId()).isNotEmpty();
      assertThat(token.getIssuedAt()).isNotNull();
      assertThat(token.getExpiration()).isNull();
      assertThat(token.getSubject()).isEqualTo("Red");
      assertThat(token.getIssuer()).isNotEmpty();
      assertThat(token.getIssuer().get()).isEqualTo("https://scm-manager.org");
    }

    @Test
    void testBuildWithEndlessJwtDisabled() {
      System.setProperty(JwtSystemProperties.ENDLESS_JWT, "false");

      JwtAccessToken token = factory.create().subject("Red").issuer("https://scm-manager.org").build();

      assertThat(token.getId()).isNotEmpty();
      assertThat(token.getIssuedAt()).isNotNull();
      assertThat(token.getExpiration()).isNotNull();
      assertThat(token.getExpiration().getTime() > token.getIssuedAt().getTime()).isTrue();
      assertThat(token.getSubject()).isEqualTo("Red");
      assertThat(token.getIssuer()).isNotEmpty();
      assertThat(token.getIssuer().get()).isEqualTo("https://scm-manager.org");
    }

    @Test
    void testBuildWithInvalidConfig() {
      System.setProperty(JwtSystemProperties.ENDLESS_JWT, "invalidStuff");

      JwtAccessToken token = factory.create().subject("Red").issuer("https://scm-manager.org").build();

      assertThat(token.getId()).isNotEmpty();
      assertThat(token.getIssuedAt()).isNotNull();
      assertThat(token.getExpiration()).isNotNull();
      assertThat(token.getExpiration().getTime() > token.getIssuedAt().getTime()).isTrue();
      assertThat(token.getSubject()).isEqualTo("Red");
      assertThat(token.getIssuer()).isNotEmpty();
      assertThat(token.getIssuer().get()).isEqualTo("https://scm-manager.org");
    }

    @Test
    void testBuildWithMissingConfig() {
      System.clearProperty(JwtSystemProperties.ENDLESS_JWT);

      JwtAccessToken token = factory.create().subject("Red").issuer("https://scm-manager.org").build();

      assertThat(token.getId()).isNotEmpty();
      assertThat(token.getIssuedAt()).isNotNull();
      assertThat(token.getExpiration()).isNotNull();
      assertThat(token.getExpiration().getTime() > token.getIssuedAt().getTime()).isTrue();
      assertThat(token.getSubject()).isEqualTo("Red");
      assertThat(token.getIssuer()).isNotEmpty();
      assertThat(token.getIssuer().get()).isEqualTo("https://scm-manager.org");
    }
  }
}
