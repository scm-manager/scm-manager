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
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Person;
import sonia.scm.security.NotPublicKeyException;
import sonia.scm.security.PublicKeyCreatedEvent;
import sonia.scm.security.PublicKeyDeletedEvent;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryDataStoreFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PublicKeyStoreTest {

  @Mock
  private Subject subject;

  @Mock
  private ScmEventBus eventBus;

  private PublicKeyStore keyStore;
  private final DataStoreFactory dataStoreFactory = new InMemoryDataStoreFactory();

  @BeforeEach
  void setUpKeyStore() {
    keyStore = new PublicKeyStore(dataStoreFactory, eventBus);
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
    doThrow(AuthorizationException.class).when(subject).checkPermission("user:changePublicKeys:zaphod");
    String rawKey = GPGTestHelper.readResourceAsString("single.asc");

    assertThrows(AuthorizationException.class, () -> keyStore.add("zaphods key", "zaphod", rawKey));
  }

  @Test
  void shouldOnlyStorePublicKeys() throws IOException {
    String rawKey = GPGTestHelper.readResourceAsString("single.asc").replace("PUBLIC", "PRIVATE");

    assertThrows(NotPublicKeyException.class, () -> keyStore.add("SCM Package Key", "trillian", rawKey));
  }

  @Test
  void shouldReturnStoredKey() throws IOException {
    String rawKey = GPGTestHelper.readResourceAsString("single.asc");
    Instant now = Instant.now();

    RawGpgKey key = keyStore.add("SCM Package Key", "trillian", rawKey);
    assertThat(key.getId()).isEqualTo("0x975922F193B07D6E");
    assertThat(key.getDisplayName()).isEqualTo("SCM Package Key");
    assertThat(key.getOwner()).isEqualTo("trillian");
    assertThat(key.getCreated()).isAfterOrEqualTo(now);
    assertThat(key.getRaw()).isEqualTo(rawKey);
    assertThat(key.isReadonly()).isFalse();
    assertThat(key.getContacts()).contains(Person.toPerson("SCM Packages (signing key for packages.scm-manager.org) <scm-team@cloudogu.com>"));

    verify(eventBus).post(any(PublicKeyCreatedEvent.class));
  }

  @Test
  void shouldReturnReadonlyStoredKey() throws IOException {
    String rawKey = GPGTestHelper.readResourceAsString("single.asc");
    Instant now = Instant.now();

    RawGpgKey key = keyStore.add("SCM Package Key", "trillian", rawKey, true);
    assertThat(key.getId()).isEqualTo("0x975922F193B07D6E");
    assertThat(key.getDisplayName()).isEqualTo("SCM Package Key");
    assertThat(key.getOwner()).isEqualTo("trillian");
    assertThat(key.getCreated()).isAfterOrEqualTo(now);
    assertThat(key.getRaw()).isEqualTo(rawKey);
    assertThat(key.isReadonly()).isTrue();
    assertThat(key.getContacts()).contains(Person.toPerson("SCM Packages (signing key for packages.scm-manager.org) <scm-team@cloudogu.com>"));

    verify(eventBus).post(any(PublicKeyCreatedEvent.class));
  }

  @Test
  void shouldFindStoredKeyById() throws IOException {
    String rawKey = GPGTestHelper.readResourceAsString("single.asc");
    keyStore.add("SCM Package Key", "trillian", rawKey);
    Optional<RawGpgKey> key = keyStore.findById("0x975922F193B07D6E");
    assertThat(key).isPresent();
  }

  @Test
  void shouldDeleteKey() throws IOException {
    String rawKey = GPGTestHelper.readResourceAsString("single.asc");
    keyStore.add("SCM Package Key", "trillian", rawKey);
    Optional<RawGpgKey> key = keyStore.findById("0x975922F193B07D6E");

    assertThat(key).isPresent();

    keyStore.delete("0x975922F193B07D6E");
    key = keyStore.findById("0x975922F193B07D6E");

    assertThat(key).isNotPresent();

    verify(eventBus).post(any(PublicKeyDeletedEvent.class));
  }

  @Test()
  void shouldThrowOnDeletingReadonlyKey() throws IOException {
    String rawKey = GPGTestHelper.readResourceAsString("single.asc");
    keyStore.add("SCM Package Key", "trillian", rawKey, true);
    Optional<RawGpgKey> key = keyStore.findById("0x975922F193B07D6E");

    assertThat(key).isPresent();

    assertThrows(DeletingReadonlyKeyNotAllowedException.class, () -> keyStore.delete("0x975922F193B07D6E"));
    key = keyStore.findById("0x975922F193B07D6E");

    assertThat(key).isPresent();

    verify(eventBus, never()).post(any(PublicKeyDeletedEvent.class));
  }

  @Test()
  void shouldThrowOnOverwriteReadonlyKey() throws IOException {
    String rawKey = GPGTestHelper.readResourceAsString("single.asc");
    keyStore.add("SCM Package Key", "trillian", rawKey, true);
    Optional<RawGpgKey> key = keyStore.findById("0x975922F193B07D6E");

    assertThat(key).isPresent();

    assertThrows(DeletingReadonlyKeyNotAllowedException.class, () -> keyStore.add("Some other entry with same raw key", "trillian", rawKey, false));

    verify(eventBus, never()).post(any(PublicKeyDeletedEvent.class));
  }

  @Test
  void shouldReturnEmptyListIfNoKeysAvailable() {
    List<RawGpgKey> keys = keyStore.findByUsername("zaphod");

    assertThat(keys)
      .isInstanceOf(List.class)
      .isEmpty();
  }

  @Test
  void shouldFindAllKeysForUser() throws IOException {
    String singleKey = GPGTestHelper.readResourceAsString("single.asc");
    keyStore.add("SCM Single Key", "trillian", singleKey);

    String multiKey = GPGTestHelper.readResourceAsString("subkeys.asc");
    keyStore.add("SCM Multi Key", "trillian", multiKey);

    List<RawGpgKey> keys = keyStore.findByUsername("trillian");

    assertThat(keys.size()).isEqualTo(2);
  }

}
