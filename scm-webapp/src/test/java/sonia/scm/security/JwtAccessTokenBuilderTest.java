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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.Sets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit test for {@link JwtAccessTokenBuilder}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class JwtAccessTokenBuilderTest {
  
  @Mock
  private KeyGenerator keyGenerator;
  
  @Mock
  private SecureKeyResolver secureKeyResolver;
  
  private Set<TokenClaimsEnricher> enrichers;
  
  private JwtAccessTokenBuilder builder;
  
  @Rule
  public ShiroRule shiro = new ShiroRule();
  
  /**
   * Prepare mocks and set up object under test.
   */
  @Before
  public void setUpObjectUnderTest() {
    when(keyGenerator.createKey()).thenReturn("42");
    when(secureKeyResolver.getSecureKey(anyString())).thenReturn(createSecureKey());
    enrichers = Sets.newHashSet();
    JwtAccessTokenBuilderFactory factory = new JwtAccessTokenBuilderFactory(keyGenerator, secureKeyResolver, enrichers);
    builder = factory.create();
  }  
  
  /**
   * Tests {@link JwtAccessTokenBuilder#build()} with subject from shiro context.
   */
  @Test
  @SubjectAware(
    configuration = "classpath:sonia/scm/shiro-001.ini",
    username = "trillian",
    password = "secret"
  )
  public void testBuildWithoutSubject() {
    JwtAccessToken token = builder.build();
    assertEquals("trillian", token.getSubject());
  }
  
  /**
   * Tests {@link JwtAccessTokenBuilder#build()} with explicit subject.
   */
  @Test
  public void testBuildWithSubject() {
    JwtAccessToken token = builder.subject("dent").build();
    assertEquals("dent", token.getSubject());
  }
  
  /**
   * Tests {@link JwtAccessTokenBuilder#build()} with enricher.
   */
  @Test
  public void testBuildWithEnricher() {
    enrichers.add((claims) -> claims.put("c", "d"));
    JwtAccessToken token = builder.subject("dent").build();
    assertEquals("d", token.getCustom("c").get());
  }
  
  /**
   * Tests {@link JwtAccessTokenBuilder#build()}.
   */
  @Test
  public void testBuild(){
    JwtAccessToken token = builder.subject("dent")
      .issuer("https://www.scm-manager.org")
      .expiresIn(5, TimeUnit.SECONDS)
      .custom("a", "b")
      .scope(Scope.valueOf("repo:*"))
      .build();
    
    // assert claims
    assertClaims(token);
    
    // reparse and assert again
    String compact = token.compact();
    assertThat(compact, not(isEmptyOrNullString()));
    Claims claims = Jwts.parser()
      .setSigningKey(secureKeyResolver.getSecureKey("dent").getBytes())
      .parseClaimsJws(compact)
      .getBody();
    assertClaims(new JwtAccessToken(claims, compact));
  }
  
  private void assertClaims(JwtAccessToken token){
    assertThat(token.getId(), not(isEmptyOrNullString()));
    assertNotNull( token.getIssuedAt() );
    assertNotNull( token.getExpiration());
    assertTrue(token.getExpiration().getTime() > token.getIssuedAt().getTime());
    assertEquals("dent", token.getSubject());
    assertTrue(token.getIssuer().isPresent());
    assertEquals(token.getIssuer().get(), "https://www.scm-manager.org");
    assertEquals("b", token.getCustom("a").get());
    assertEquals("[\"repo:*\"]", token.getScope().toString());
  }
  
  private SecureKey createSecureKey() {
    byte[] bytes = new byte[32];
    new Random().nextBytes(bytes);
    return new SecureKey(bytes, System.currentTimeMillis());
  }

}