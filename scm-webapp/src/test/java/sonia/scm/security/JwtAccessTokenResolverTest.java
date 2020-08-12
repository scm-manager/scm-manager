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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.apache.shiro.authc.AuthenticationException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;
import java.util.Set;

import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static sonia.scm.security.SecureKeyTestUtil.createSecureKey;

/**
 * Unit tests for {@link JwtAccessTokenResolver}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtAccessTokenResolverTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Mock
  private SecureKeyResolver keyResolver;
  
  @Mock
  private AccessTokenValidator validator;
  
  private JwtAccessTokenResolver resolver;
  
  /**
   * Prepares the object under test.
   */
  @Before
  public void prepareObjectUnderTest() {
    Set<AccessTokenValidator> validators = Sets.newHashSet(validator);
    when(validator.validate(Mockito.any(AccessToken.class))).thenReturn(true);
    resolver = new JwtAccessTokenResolver(keyResolver, validators);
  }

  /**
   * Tests {@link JwtAccessTokenResolver#resolve(BearerToken)}.
   */
  @Test
  public void testResolve() {
    SecureKey secureKey = createSecureKey();
    resolveKey(secureKey);
    String compact = createCompactToken("marvin", secureKey);

    BearerToken bearer = BearerToken.valueOf(compact);
    JwtAccessToken access = resolver.resolve(bearer);
    assertEquals("marvin", access.getSubject());
  }
  
  /**
   * Tests {@link JwtAccessTokenResolver#resolve(BearerToken)} with a failed validator.
   */
  @Test
  public void testResolveWithFailedValidator() {
    SecureKey secureKey = createSecureKey();
    resolveKey(secureKey);
    String compact = createCompactToken("marvin", secureKey);

    // prepare mock
    when(validator.validate(Mockito.any(AccessToken.class))).thenReturn(false);
    
    // expect exception
    expectedException.expect(AuthenticationException.class);
    expectedException.expectMessage(Matchers.containsString("token"));
    
    BearerToken bearer = BearerToken.valueOf(compact);
    resolver.resolve(bearer);
  }
  
  /**
   * Tests {@link JwtAccessTokenResolver#resolve(BearerToken)} with a expired token.
   */
  @Test
  public void testResolveWithExpiredToken() {
    SecureKey secureKey = createSecureKey();
    resolveKey(secureKey);
    
    // create expired token
    Date exp = new Date(System.currentTimeMillis() - 600l);
    String compact = createCompactToken("trillian", secureKey, exp, Scope.empty());
    
    // expect exception
    expectedException.expect(TokenExpiredException.class);
    expectedException.expectCause(instanceOf(ExpiredJwtException.class));
    
    BearerToken bearer = BearerToken.valueOf(compact);
    resolver.resolve(bearer);
  }
  
  /**
   * Tests {@link JwtAccessTokenResolver#resolve(BearerToken)} with invalid signature.
   */
  @Test
  public void testResolveWithInvalidSignature() {
    resolveKey(createSecureKey());
    
    // create expired token
    String compact = createCompactToken("trillian", createSecureKey());
    
    // expect exception
    expectedException.expect(AuthenticationException.class);
    expectedException.expectCause(instanceOf(SignatureException.class));
    
    BearerToken bearer = BearerToken.valueOf(compact);
    resolver.resolve(bearer);
  }
  
  /**
   * Tests {@link JwtAccessTokenResolver#resolve(BearerToken)} without signature.
   */
  @Test
  public void testResolveWithoutSignature() {
    String compact = Jwts.builder().setSubject("test").compact();
    
    // expect exception
    expectedException.expect(AuthenticationException.class);
    expectedException.expectCause(instanceOf(UnsupportedJwtException.class));
    
    BearerToken bearer = BearerToken.valueOf(compact);
    resolver.resolve(bearer);
  }
  
  /**
   * Tests {@link JwtAccessTokenResolver#resolve(BearerToken)} with scope.
   */
  @Test
  public void testResolveWithScope() {
    SecureKey key = createSecureKey();
    resolveKey(key);

    String compact = createCompactToken(
      "marvin", 
      key, 
      new Date(System.currentTimeMillis() + 60000), 
      Scope.valueOf("repo:*", "user:*")
    );
    
    BearerToken bearer = BearerToken.valueOf(compact);
    AccessToken access = resolver.resolve(bearer);
    Scope scope = access.getScope();
    assertThat(scope, Matchers.containsInAnyOrder("repo:*", "user:*"));
  }
  
  private String createCompactToken(String subject, SecureKey key) {
    return createCompactToken(subject, key, Scope.empty());
  }
  
  private String createCompactToken(String subject, SecureKey key, Scope scope) {
    return createCompactToken(subject, key, new Date(System.currentTimeMillis() + 60000), scope);
  }

  private String createCompactToken(String subject, SecureKey key, Date exp, Scope scope) {
    return Jwts.builder()
      .claim(Scopes.CLAIMS_KEY, ImmutableList.copyOf(scope))
      .setSubject(subject)
      .setExpiration(exp)
      .signWith(SignatureAlgorithm.HS256, key.getBytes())
      .compact();
  }
  
  private void resolveKey(SecureKey key) {
    when(
      keyResolver.resolveSigningKey(
        Mockito.any(JwsHeader.class), 
        Mockito.any(Claims.class)
      )
    )
    .thenReturn(
      new SecretKeySpec(
        key.getBytes(), 
        SignatureAlgorithm.HS256.getJcaName()
      )
    );
  }


}
