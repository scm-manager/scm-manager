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

package sonia.scm.security.gpg;

import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.NotPublicKeyException;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class PublicKeyStoreTest {

  @Mock
  private Subject subject;

  private PublicKeyStore keyStore;
  private final DataStoreFactory dataStoreFactory = new InMemoryDataStoreFactory();

  @BeforeEach
  void setUpKeyStore() {
    keyStore = new PublicKeyStore(dataStoreFactory);
  }

  @BeforeEach
  void bindSubject() {
    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDownSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldThrowAuthorizationExceptionOnAdd() throws IOException {
    doThrow(AuthorizationException.class).when(subject).checkPermission("user:modify:zaphod");
    String rawKey = GPGTestHelper.readKey("single.asc");

    assertThrows(AuthorizationException.class, () -> keyStore.add("zaphods key", "zaphod", rawKey));
  }

  @Test
  void shouldOnlyStorePublicKeys() throws IOException {
    String rawKey = GPGTestHelper.readKey("single.asc").replace("PUBLIC", "PRIVATE");

    assertThrows(NotPublicKeyException.class, () -> keyStore.add("SCM Package Key", "trillian", rawKey));
  }

  @Test
  void shouldReturnStoredKey() throws IOException {
    String rawKey = GPGTestHelper.readKey("single.asc");
    Instant now = Instant.now();

    RawGpgKey key = keyStore.add("SCM Package Key", "trillian", rawKey);
    assertThat(key.getId()).isEqualTo("0x975922F193B07D6E");
    assertThat(key.getDisplayName()).isEqualTo("SCM Package Key");
    assertThat(key.getOwner()).isEqualTo("trillian");
    assertThat(key.getCreated()).isAfterOrEqualTo(now);
    assertThat(key.getRaw()).isEqualTo(rawKey);
  }

  @Test
  void shouldFindStoredKeyById() throws IOException {
    String rawKey = GPGTestHelper.readKey("single.asc");
    keyStore.add("SCM Package Key", "trillian", rawKey);
    Optional<RawGpgKey> key = keyStore.findById("0x975922F193B07D6E");
    assertThat(key).isPresent();
  }

  @Test
  void shouldDeleteKey() throws IOException {
    String rawKey = GPGTestHelper.readKey("single.asc");
    keyStore.add("SCM Package Key", "trillian", rawKey);
    Optional<RawGpgKey> key = keyStore.findById("0x975922F193B07D6E");

    assertThat(key).isPresent();

    keyStore.delete("0x975922F193B07D6E");
    key = keyStore.findById("0x975922F193B07D6E");

    assertThat(key).isNotPresent();
  }

  @Test
  void shouldReturnEmptyListIfNoKeysAvailable() {
    List<RawGpgKey> keys = keyStore.findByUsername("zaphod");

    assertThat(keys).isEmpty();
    assertThat(keys).isInstanceOf(List.class);
  }

  @Test
  void shouldFindAllKeysForUser() throws IOException {
    String singleKey = GPGTestHelper.readKey("single.asc");
    keyStore.add("SCM Single Key", "trillian", singleKey);

    String multiKey = GPGTestHelper.readKey("subkeys.asc");
    keyStore.add("SCM Multi Key", "trillian", multiKey);

    List<RawGpgKey> keys = keyStore.findByUsername("trillian");

    assertThat(keys.size()).isEqualTo(2);
  }

}
