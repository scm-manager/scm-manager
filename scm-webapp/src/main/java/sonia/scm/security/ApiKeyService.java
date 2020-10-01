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

import com.google.common.util.concurrent.Striped;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.util.ThreadContext;
import sonia.scm.ContextEntry;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.RandomStringUtils.random;
import static sonia.scm.AlreadyExistsException.alreadyExists;

public class ApiKeyService {

  public static final int KEY_LENGTH = 20;

  private final DataStore<ApiKeyCollection> store;
  private final PasswordService passwordService;
  private final KeyGenerator keyGenerator;
  private final Supplier<String> passphraseGenerator;
  private final ApiKeyTokenHandler tokenHandler;

  private final Striped<ReadWriteLock> locks = Striped.readWriteLock(10);

  @Inject
  ApiKeyService(DataStoreFactory storeFactory, KeyGenerator keyGenerator, PasswordService passwordService, ApiKeyTokenHandler tokenHandler) {
    this(storeFactory, passwordService, keyGenerator, tokenHandler, () -> random(KEY_LENGTH, 0, 0, true, true, null, new SecureRandom()));
  }

  ApiKeyService(DataStoreFactory storeFactory, PasswordService passwordService, KeyGenerator keyGenerator, ApiKeyTokenHandler tokenHandler, Supplier<String> passphraseGenerator) {
    this.store = storeFactory.withType(ApiKeyCollection.class).withName("apiKeys").build();
    this.passwordService = passwordService;
    this.keyGenerator = keyGenerator;
    this.tokenHandler = tokenHandler;
    this.passphraseGenerator = passphraseGenerator;
  }

  public CreationResult createNewKey(String name, String permissionRole) {
    String user = currentUser();
    UserPermissions.changeApiKeys(user).check();
    String passphrase = passphraseGenerator.get();
    String hashedPassphrase = passwordService.encryptPassword(passphrase);
    final String id = keyGenerator.createKey();
    ApiKeyWithPassphrase key = new ApiKeyWithPassphrase(id, name, permissionRole, hashedPassphrase, now());
    Lock lock = locks.get(user).writeLock();
    lock.lock();
    try {
      if (containsName(user, name)) {
        throw alreadyExists(ContextEntry.ContextBuilder.entity(ApiKeyWithPassphrase.class, name));
      }
      final ApiKeyCollection apiKeyCollection = store.getOptional(user).orElse(new ApiKeyCollection(emptyList()));
      final ApiKeyCollection newApiKeyCollection = apiKeyCollection.add(key);
      store.put(user, newApiKeyCollection);
    } finally {
      lock.unlock();
    }
    final String token = tokenHandler.createToken(user, new ApiKey(key), passphrase);
    return new CreationResult(token, id);
  }

  public void remove(String id) {
    String user = currentUser();
    UserPermissions.changeApiKeys(user).check();
    Lock lock = locks.get(user).writeLock();
    lock.lock();
    try {
      if (!containsId(user, id)) {
        return;
      }
      store.getOptional(user).ifPresent(
        apiKeyCollection -> {
          final ApiKeyCollection newApiKeyCollection = apiKeyCollection.remove(key -> id.equals(key.getId()));
          store.put(user, newApiKeyCollection);
        }
      );
    } finally {
      lock.unlock();
    }
  }

  CheckResult check(String tokenAsString) {
    return check(tokenHandler.readToken(tokenAsString)
      .orElseThrow(AuthorizationException::new));
  }

  private CheckResult check(ApiKeyTokenHandler.Token token) {
    return check(token.getUser(), token.getApiKeyId(), token.getPassphrase());
  }

  CheckResult check(String user, String id, String passphrase) {
    Lock lock = locks.get(user).readLock();
    lock.lock();
    try {
      return store
        .get(user)
        .getKeys()
        .stream()
        .filter(key -> key.getId().equals(id))
        .filter(key -> passwordService.passwordsMatch(passphrase, key.getPassphrase()))
        .map(ApiKeyWithPassphrase::getPermissionRole)
        .map(role -> new CheckResult(user, role))
        .findAny()
        .orElseThrow(AuthorizationException::new);
    } finally {
      lock.unlock();
    }
  }

  public Collection<ApiKey> getKeys() {
    return store.getOptional(currentUser())
      .map(ApiKeyCollection::getKeys)
      .map(Collection::stream)
      .orElse(Stream.empty())
      .map(ApiKey::new)
      .collect(toList());
  }

  private String currentUser() {
    return ThreadContext.getSubject().getPrincipals().getPrimaryPrincipal().toString();
  }

  private boolean containsId(String user, String id) {
    return store
      .getOptional(user)
      .map(ApiKeyCollection::getKeys)
      .orElse(emptyList())
      .stream()
      .anyMatch(key -> key.getId().equals(id));
  }

  private boolean containsName(String user, String name) {
    return store
      .getOptional(user)
      .map(ApiKeyCollection::getKeys)
      .orElse(emptyList())
      .stream()
      .anyMatch(key -> key.getDisplayName().equals(name));
  }

  @Getter
  @AllArgsConstructor
  public static class CreationResult {
    private final String token;
    private final String id;
  }

  @Getter
  @AllArgsConstructor
  public static class CheckResult {
    private final String user;
    private final String permissionRole;
  }
}
