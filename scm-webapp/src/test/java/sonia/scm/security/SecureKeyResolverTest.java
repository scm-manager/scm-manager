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


import io.jsonwebtoken.Jwts;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.lifecycle.jwt.JwtSettings;
import sonia.scm.lifecycle.jwt.JwtSettingsStore;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class SecureKeyResolverTest
{

   @Test
  public void testGetSecureKey()
  {
    SecureKey key = resolver.getSecureKey("test");

    assertNotNull(key);
    when(store.get("test")).thenReturn(key);
    when(jwtSettingsStore.get()).thenReturn(settings);

    SecureKey sameKey = resolver.getSecureKey("test");

    assertSame(key, sameKey);
  }

  @Test
  public void shouldReturnRegeneratedKey() {
    when(jwtSettingsStore.get()).thenReturn(settings);
    SecureKey expiredKey = new SecureKey("oldKey".getBytes(), 0);
    when(store.get("test")).thenReturn(expiredKey);

    SecureKey regeneratedKey = resolver.getSecureKey("test");
    assertThat(Arrays.equals(regeneratedKey.getBytes(), expiredKey.getBytes())).isFalse();
    assertThat(regeneratedKey.getCreationDate() > settings.getKeysValidAfterTimestampInMs()).isTrue();


    when(store.get("test")).thenReturn(regeneratedKey);
    SecureKey sameRegeneratedKey = resolver.getSecureKey("test");
    assertThat(Arrays.equals(sameRegeneratedKey.getBytes(), regeneratedKey.getBytes())).isTrue();
    assertThat(sameRegeneratedKey.getCreationDate()).isEqualTo(regeneratedKey.getCreationDate());
  }

   @Test
  public void testResolveSigningKeyBytes()
  {
    SecureKey key = resolver.getSecureKey("test");

    when(store.get("test")).thenReturn(key);
    when(jwtSettingsStore.get()).thenReturn(settings);

    byte[] bytes = resolver.resolveSigningKeyBytes(null,
                     Jwts.claims().setSubject("test"));

    assertArrayEquals(key.getBytes(), bytes);
  }

   @Test
  public void testResolveSigningKeyBytesWithoutKey()
  {
    byte[] bytes = resolver.resolveSigningKeyBytes(null, Jwts.claims().setSubject("test"));
    assertThat(bytes[0]).isEqualTo((byte) 42);
  }

   @Test(expected = IllegalArgumentException.class)
  public void testResolveSigningKeyBytesWithoutSubject()
  {
    resolver.resolveSigningKeyBytes(null, Jwts.claims());
  }


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
    resolver = new SecureKeyResolver(factory, jwtSettingsStore, random);
  }

  //~--- fields ---------------------------------------------------------------

  private SecureKeyResolver resolver;

  @Mock
  private ConfigurationEntryStore<SecureKey> store;

  @Mock
  private JwtSettingsStore jwtSettingsStore;

  private JwtSettings settings = new JwtSettings(false, 100);
}
