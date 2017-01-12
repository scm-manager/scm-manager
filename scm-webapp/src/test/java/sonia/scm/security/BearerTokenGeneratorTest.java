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

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Sets;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import static org.hamcrest.Matchers.*;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.security.SecureRandom;
import java.util.Set;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class BearerTokenGeneratorTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testCreateBearerToken()
  {
    User trillian = UserTestData.createTrillian();
    SecureKey key = createSecureKey();

    when(keyGenerator.createKey()).thenReturn("sid");
    when(keyResolver.getSecureKey(trillian.getName())).thenReturn(key);

    String token = tokenGenerator.createBearerToken(trillian);

    assertThat(token, not(isEmptyOrNullString()));
    assertTrue(Jwts.parser().isSigned(token));

    Claims claims = Jwts.parser().setSigningKey(key.getBytes()).parseClaimsJws(
                      token).getBody();

    assertEquals(trillian.getName(), claims.getSubject());
    assertEquals("sid", claims.getId());
    assertEquals("123", claims.get("abc"));
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
    Set<TokenClaimsEnricher> enrichers = Sets.newHashSet();
    enrichers.add((claims) -> {claims.put("abc", "123");});
    tokenGenerator = new BearerTokenGenerator(keyGenerator, keyResolver, enrichers);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private SecureKey createSecureKey()
  {
    byte[] bytes = new byte[32];

    random.nextBytes(bytes);

    return new SecureKey(bytes, System.currentTimeMillis());
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final SecureRandom random = new SecureRandom();

  /** Field description */
  @Mock
  private KeyGenerator keyGenerator;

  /** Field description */
  @Mock
  private SecureKeyResolver keyResolver;

  /** Field description */
  private BearerTokenGenerator tokenGenerator;
}
