/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class SecureKeyResolverTest {

  private SecureKeyResolver resolver;
  @Mock
  private ConfigurationEntryStore<SecureKey> store;
  @Mock
  private JwtSettingsStore jwtSettingsStore;
  private final JwtSettings settings = new JwtSettings(false, 100);

  @Test
  public void testGetSecureKey() {
    SecureKey key = resolver.getSecureKey("test");

    assertNotNull(key);
    when(store.get("test")).thenReturn(key);
    when(jwtSettingsStore.get()).thenReturn(settings);

    SecureKey sameKey = resolver.getSecureKey("test");

    assertSame(key, sameKey);
  }

  @Test
  public void clearSecureKey() {
    resolver.deleteStore();
    verify(store).clear();
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
  public void testResolveSigningKeyBytes() {
    SecureKey key = resolver.getSecureKey("test");

    when(store.get("test")).thenReturn(key);
    when(jwtSettingsStore.get()).thenReturn(settings);

    byte[] bytes = resolver.resolveSigningKeyBytes(null,
      Jwts.claims().setSubject("test"));

    assertArrayEquals(key.getBytes(), bytes);
  }

  @Test
  public void testResolveSigningKeyBytesWithoutKey() {
    byte[] bytes = resolver.resolveSigningKeyBytes(null, Jwts.claims().setSubject("test"));
    assertThat(bytes[0]).isEqualTo((byte) 42);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testResolveSigningKeyBytesWithoutSubject() {
    resolver.resolveSigningKeyBytes(null, Jwts.claims());
  }

  @Before
  public void setUp() {
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
}
