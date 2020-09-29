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
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.util.ThreadContext;
import sonia.scm.ContextEntry;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.ConfigurationEntryStoreFactory;

import javax.inject.Inject;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.RandomStringUtils.random;
import static sonia.scm.AlreadyExistsException.alreadyExists;

public class ApiKeyService {

  public static final int KEY_LENGTH = 20;

  private final ConfigurationEntryStore<ApiKeyCollection> store;
  private final PasswordService passwordService;
  private final KeyGenerator keyGenerator;
  private final Supplier<String> passphraseGenerator;

  private final Striped<ReadWriteLock> locks = Striped.readWriteLock(10);

  @Inject
  ApiKeyService(ConfigurationEntryStoreFactory storeFactory, KeyGenerator keyGenerator, PasswordService passwordService) {
    this(storeFactory, passwordService, keyGenerator, () -> random(KEY_LENGTH, 0, 0, true, true, null, new SecureRandom()));
  }

  ApiKeyService(ConfigurationEntryStoreFactory storeFactory, PasswordService passwordService, KeyGenerator keyGenerator, Supplier<String> passphraseGenerator) {
    this.store = storeFactory.withType(ApiKeyCollection.class).withName("apiKeys").build();
    this.passwordService = passwordService;
    this.keyGenerator = keyGenerator;
    this.passphraseGenerator = passphraseGenerator;
  }

  public String createNewKey(String name, String role) {
    String user = currentUser();
    String passphrase = passphraseGenerator.get();
    String hashedPassphrase = passwordService.encryptPassword(passphrase);
    Lock lock = locks.get(user).writeLock();
    lock.lock();
    try {
      if (containsName(user, name)) {
        throw alreadyExists(ContextEntry.ContextBuilder.entity(ApiKeyWithPassphrase.class, name));
      }
      final ApiKeyCollection apiKeyCollection = store.getOptional(user).orElse(new ApiKeyCollection(emptyList()));
      final ApiKeyCollection newApiKeyCollection = apiKeyCollection.add(new ApiKeyWithPassphrase(keyGenerator.createKey(), name, role, hashedPassphrase));
      store.put(user, newApiKeyCollection);
    } finally {
      lock.unlock();
    }
    return passphrase;
  }

  public void remove(String id) {
    String user = currentUser();
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

  Optional<String> check(String user, String id, String passphrase) {
    Lock lock = locks.get(user).readLock();
    lock.lock();
    try {
      return store
        .get(user)
        .getKeys()
        .stream()
        .filter(key -> key.getId().equals(id))
        .filter(key -> passwordService.passwordsMatch(passphrase, key.getPassphrase()))
        .map(ApiKeyWithPassphrase::getRole)
        .findAny();
    } finally {
      lock.unlock();
    }
  }

  public Collection<ApiKey> getKeys() {
    return store.get(currentUser()).getKeys().stream().map(ApiKey::new).collect(toList());
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
}
