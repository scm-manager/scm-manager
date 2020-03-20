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

//~--- non-JDK imports --------------------------------------------------------

import io.jsonwebtoken.Jwts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class SecureKeyResolverTest
{

  /**
   * Method description
   *
   */
  @Test
  public void testGetSecureKey()
  {
    SecureKey key = resolver.getSecureKey("test");

    assertNotNull(key);
    when(store.get("test")).thenReturn(key);

    SecureKey sameKey = resolver.getSecureKey("test");

    assertSame(key, sameKey);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testResolveSigningKeyBytes()
  {
    SecureKey key = resolver.getSecureKey("test");

    when(store.get("test")).thenReturn(key);

    byte[] bytes = resolver.resolveSigningKeyBytes(null,
                     Jwts.claims().setSubject("test"));

    assertArrayEquals(key.getBytes(), bytes);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testResolveSigningKeyBytesWithoutKey()
  {
    byte[] bytes = resolver.resolveSigningKeyBytes(null, Jwts.claims().setSubject("test"));
    assertThat(bytes[0]).isEqualTo((byte) 42);
  }

  /**
   * Method description
   *
   */
  @Test(expected = IllegalArgumentException.class)
  public void testResolveSigningKeyBytesWithoutSubject()
  {
    resolver.resolveSigningKeyBytes(null, Jwts.claims());
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   */
  @Before
  public void setUp()
  {
    ConfigurationEntryStoreFactory factory = mock(ConfigurationEntryStoreFactory.class);

    when(factory.withType(any())).thenCallRealMethod();
    when(factory.<SecureKey>getStore(argThat(storeParameters -> {
      assertThat(storeParameters.getName()).isEqualTo(SecureKeyResolver.STORE_NAME);
      assertThat(storeParameters.getType()).isEqualTo(SecureKey.class);
      return true;
    }))).thenReturn(store);
    Random random = mock(Random.class);
    doAnswer(invocation -> ((byte[]) invocation.getArguments()[0])[0] = 42).when(random).nextBytes(any());
    resolver = new SecureKeyResolver(factory, random);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private SecureKeyResolver resolver;

  /** Field description */
  @Mock
  private ConfigurationEntryStore<SecureKey> store;
}
