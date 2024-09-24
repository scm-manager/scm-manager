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

import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.AlreadyExistsException;
import sonia.scm.HandlerEventType;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryDataStore;
import sonia.scm.store.InMemoryDataStoreFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserEvent;

import java.util.Optional;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiKeyServiceTest {

  int nextKey = 1;
  int nextId = 1;

  PasswordService passwordService = mock(PasswordService.class);
  Supplier<String> passphraseGenerator = () -> Integer.toString(nextKey++);
  KeyGenerator keyGenerator = () -> Integer.toString(nextId++);
  ApiKeyTokenHandler tokenHandler = new ApiKeyTokenHandler();
  DataStoreFactory storeFactory = new InMemoryDataStoreFactory(new InMemoryDataStore<ApiKeyCollection>());
  DataStore<ApiKeyCollection> store = storeFactory.withType(ApiKeyCollection.class).withName("apiKeys").build();
  ScmConfiguration scmConfiguration = new ScmConfiguration();
  ApiKeyService service = new ApiKeyService(storeFactory, passwordService, keyGenerator, tokenHandler, passphraseGenerator, scmConfiguration);

  @BeforeEach
  void mockPasswordService() {
    when(passwordService.encryptPassword(any()))
      .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0) + "-hashed");
    when(passwordService.passwordsMatch(any(), any()))
      .thenAnswer(invocationOnMock -> invocationOnMock.getArgument(1, String.class).startsWith(invocationOnMock.getArgument(0)));
  }

  @Nested
  class WithLoggedInUser {
    @BeforeEach
    void mockUser() {
      final Subject subject = mock(Subject.class);
      ThreadContext.bind(subject);
      final PrincipalCollection principalCollection = mock(PrincipalCollection.class);
      when(subject.getPrincipals()).thenReturn(principalCollection);
      when(principalCollection.getPrimaryPrincipal()).thenReturn("dent");
    }

    @AfterEach
    void unbindSubject() {
      ThreadContext.unbindSubject();
    }

    @Test
    void shouldCreateNewKeyAndStoreItHashed() {
      service.createNewKey("dent","1", "READ");

      ApiKeyCollection apiKeys = store.get("dent");

      assertThat(apiKeys.getKeys()).hasSize(1);
      ApiKeyWithPassphrase key = apiKeys.getKeys().iterator().next();
      assertThat(key.getPermissionRole()).isEqualTo("READ");
      assertThat(key.getPassphrase()).isEqualTo("1-hashed");

      ApiKeyService.CheckResult role = service.check("dent", "1",  "1-hashed");

      assertThat(role).extracting("permissionRole").isEqualTo("READ");
    }

    @Test
    void shouldReturnRoleForKey() {
      String newKey = service.createNewKey("dent","1", "READ").getToken();

      Optional<ApiKeyService.CheckResult> role = service.check(newKey);

      assertThat(role).get().extracting("permissionRole").isEqualTo("READ");
    }

    @Test
    void shouldHandleNewUser() {
      assertThat(service.getKeys("zaphod")).isEmpty();
    }

    @Test
    void shouldNotReturnAnythingWithWrongKey() {
      service.createNewKey("dent","1", "READ");

      assertThrows(AuthorizationException.class, () -> service.check("dent", "1", "wrong"));
    }

    @Test
    void shouldAddSecondKey() {
      ApiKeyService.CreationResult firstKey = service.createNewKey("dent","1", "READ");
      ApiKeyService.CreationResult secondKey = service.createNewKey("dent","2", "WRITE");

      ApiKeyCollection apiKeys = store.get("dent");

      assertThat(apiKeys.getKeys()).hasSize(2);

      assertThat(service.check(firstKey.getToken())).get().extracting("permissionRole").isEqualTo("READ");
      assertThat(service.check(secondKey.getToken())).get().extracting("permissionRole").isEqualTo("WRITE");

      assertThat(service.getKeys("dent")).extracting("id")
        .contains(firstKey.getId(), secondKey.getId());
    }

    @Test
    void shouldRemoveKey() {
      String firstKey = service.createNewKey("dent","first", "READ").getToken();
      String secondKey = service.createNewKey("dent","second", "WRITE").getToken();

      service.remove("dent","1");

      assertThrows(AuthorizationException.class, () -> service.check(firstKey));
      assertThat(service.check(secondKey)).get().extracting("permissionRole").isEqualTo("WRITE");
    }

    @Test
    void shouldFailWhenAddingSameNameTwice() {
      String firstKey = service.createNewKey("dent","1", "READ").getToken();

      assertThrows(AlreadyExistsException.class, () -> service.createNewKey("dent","1", "WRITE"));

      assertThat(service.check(firstKey)).get().extracting("permissionRole").isEqualTo("READ");
    }

    @Test
    void shouldIgnoreCorrectPassphraseWithWrongName() {
      String firstKey = service.createNewKey("dent","1", "READ").getToken();

      assertThrows(AuthorizationException.class, () -> service.check("dent", "other", firstKey));
    }

    @Test
    void shouldDeleteTokensWhenUserIsDeleted() {
      service.createNewKey("dent","1", "READ").getToken();

      assertThat(store.get("dent").getKeys()).hasSize(1);

      service.cleanupForDeletedUser(new UserEvent(HandlerEventType.DELETE, new User("dent")));

      assertThat(store.get("dent")).isNull();
    }

    @Test
    void shouldFailIfApiKeysAreDisabled() {
      scmConfiguration.setEnabledApiKeys(false);

      assertThrows(ApiKeysDisabledException.class, () -> service.createNewKey("dent", "1", "READ"));

    }
  }
}
