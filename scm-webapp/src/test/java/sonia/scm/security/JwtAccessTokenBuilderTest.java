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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.collect.Sets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.shiro.util.ThreadContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static sonia.scm.security.SecureKeyTestUtil.createSecureKey;

/**
 * Unit test for {@link JwtAccessTokenBuilder}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
@SubjectAware(
  configuration = "classpath:sonia/scm/shiro-001.ini",
  username = "trillian",
  password = "secret"
)
public class JwtAccessTokenBuilderTest {
  
  {
    ThreadContext.unbindSubject();
  }

  @Mock
  private KeyGenerator keyGenerator;
  
  @Mock
  private SecureKeyResolver secureKeyResolver;
  
  private Set<AccessTokenEnricher> enrichers;
  
  private JwtAccessTokenBuilderFactory factory;
  
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
    factory = new JwtAccessTokenBuilderFactory(keyGenerator, secureKeyResolver, enrichers);
  }
  
  /**
   * Tests {@link JwtAccessTokenBuilder#build()} with subject from shiro context.
   */
  @Test
  public void testBuildWithoutSubject() {
    JwtAccessToken token = factory.create().build();
    assertEquals("trillian", token.getSubject());
  }
  
  /**
   * Tests {@link JwtAccessTokenBuilder#build()} with explicit subject.
   */
  @Test
  public void testBuildWithSubject() {
    JwtAccessToken token = factory.create().subject("dent").build();
    assertEquals("dent", token.getSubject());
  }
  
  /**
   * Tests {@link JwtAccessTokenBuilder#build()} with enricher.
   */
  @Test
  public void testBuildWithEnricher() {
    enrichers.add((b) -> b.custom("c", "d"));
    JwtAccessToken token = factory.create().subject("dent").build();
    assertEquals("d", token.getCustom("c").get());
  }
  
  /**
   * Tests {@link JwtAccessTokenBuilder#build()}.
   */
  @Test
  public void testBuild(){
    JwtAccessToken token = factory.create().subject("dent")
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
}
