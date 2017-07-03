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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Set;
import javax.crypto.spec.SecretKeySpec;
import org.apache.shiro.authc.AuthenticationException;
import org.hamcrest.Matchers;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link JwtAccessTokenResolver}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtAccessTokenResolverTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  
  private final SecureRandom random = new SecureRandom();
  
  @Mock
  private SecureKeyResolver keyResolver;
  
  @Mock
  private TokenClaimsValidator validator;
  
  private JwtAccessTokenResolver resolver;
  
  /**
   * Prepares the object under test.
   */
  @Before
  public void prepareObjectUnderTest() {
    Set<TokenClaimsValidator> validators = Sets.newHashSet(validator);
    when(validator.validate(anyMap())).thenReturn(true);
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
    when(validator.validate(anyMap())).thenReturn(false);
    
    // expect exception
    expectedException.expect(AuthenticationException.class);
    expectedException.expectMessage(Matchers.containsString("claims"));
    
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
    Date exp = new Date(System.currentTimeMillis() - 600L);
    String compact = createCompactToken("trillian", secureKey, exp, Scope.empty());
    
    // expect exception
    expectedException.expect(AuthenticationException.class);
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
  
  private SecureKey createSecureKey() {
    byte[] bytes = new byte[32];
    random.nextBytes(bytes);
    return new SecureKey(bytes, System.currentTimeMillis());
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
        SignatureAlgorithm.HS256.getValue()
      )
    );
  }


}
