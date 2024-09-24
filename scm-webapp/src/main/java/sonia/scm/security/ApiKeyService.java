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

import com.github.legman.Subscribe;
import com.google.common.util.concurrent.Striped;
import jakarta.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.shiro.authc.credential.PasswordService;
import org.apache.shiro.authz.AuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.HandlerEventType;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.user.UserEvent;
import sonia.scm.user.UserPermissions;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.time.Instant.now;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.RandomStringUtils.random;
import static sonia.scm.AlreadyExistsException.alreadyExists;

@SuppressWarnings("UnstableApiUsage")
public class ApiKeyService {

  private static final Logger LOG = LoggerFactory.getLogger(ApiKeyService.class);
  private static final int PASSPHRASE_LENGTH = 20;

  private final DataStore<ApiKeyCollection> store;
  private final PasswordService passwordService;
  private final KeyGenerator keyGenerator;
  private final Supplier<String> passphraseGenerator;
  private final ApiKeyTokenHandler tokenHandler;
  private final ScmConfiguration scmConfiguration;

  private final Striped<ReadWriteLock> locks = Striped.readWriteLock(10);

  @Inject
  ApiKeyService(DataStoreFactory storeFactory, KeyGenerator keyGenerator, PasswordService passwordService, ApiKeyTokenHandler tokenHandler, ScmConfiguration scmConfiguration) {
    this(storeFactory, passwordService, keyGenerator, tokenHandler, () -> random(PASSPHRASE_LENGTH, 0, 0, true, true, null, new SecureRandom()), scmConfiguration);
  }

  ApiKeyService(DataStoreFactory storeFactory, PasswordService passwordService, KeyGenerator keyGenerator, ApiKeyTokenHandler tokenHandler, Supplier<String> passphraseGenerator, ScmConfiguration scmConfiguration) {
    this.store = storeFactory.withType(ApiKeyCollection.class).withName("apiKeys").build();
    this.passwordService = passwordService;
    this.keyGenerator = keyGenerator;
    this.tokenHandler = tokenHandler;
    this.passphraseGenerator = passphraseGenerator;
    this.scmConfiguration = scmConfiguration;
  }

  public CreationResult createNewKey(String username, String keyDisplayName, String permissionRole) {
    UserPermissions.changeApiKeys(username).check();
    if (!scmConfiguration.isEnabledApiKeys()) {
      throw new ApiKeysDisabledException();
    }
    String passphrase = passphraseGenerator.get();
    String hashedPassphrase = passwordService.encryptPassword(passphrase);
    String id = keyGenerator.createKey();
    ApiKeyWithPassphrase key = new ApiKeyWithPassphrase(id, keyDisplayName, permissionRole, hashedPassphrase, now());
    doSynchronized(username, true, () -> {
      persistKey(keyDisplayName, username, key);
      return null;
    });
    String token = tokenHandler.createToken(username, new ApiKey(key), passphrase);
    LOG.info("created new api key for user {} with role {}", username, permissionRole);
    return new CreationResult(token, id);
  }

  public void persistKey(String name, String user, ApiKeyWithPassphrase key) {
    if (containsName(user, name)) {
      throw alreadyExists(ContextEntry.ContextBuilder.entity(ApiKeyWithPassphrase.class, name));
    }
    ApiKeyCollection apiKeyCollection = store.getOptional(user).orElse(new ApiKeyCollection(emptyList()));
    ApiKeyCollection newApiKeyCollection = apiKeyCollection.add(key);
    store.put(user, newApiKeyCollection);
  }

  public void remove(String username, String id) {
    UserPermissions.changeApiKeys(username).check();
    doSynchronized(username, true, () -> {
      if (!containsId(username, id)) {
        return null;
      }
      store.getOptional(username).ifPresent(
        apiKeyCollection -> {
          ApiKeyCollection newApiKeyCollection = apiKeyCollection.remove(key -> id.equals(key.getId()));
          store.put(username, newApiKeyCollection);
          LOG.info("removed api key for user {}", username);
        }
      );
      return null;
    });
  }

  Optional<CheckResult> check(String tokenAsString) {
    return tokenHandler.readToken(tokenAsString).map(this::check);
  }

  private CheckResult check(ApiKeyTokenHandler.Token token) {
    return check(token.getUser(), token.getApiKeyId(), token.getPassphrase());
  }

  CheckResult check(String user, String id, String passphrase) {
    return doSynchronized(user, false, () -> store
      .get(user)
      .getKeys()
      .stream()
      .filter(key -> key.getId().equals(id))
      .filter(key -> passwordsMatch(user, passphrase, key))
      .map(ApiKeyWithPassphrase::getPermissionRole)
      .map(role -> new CheckResult(user, role))
      .findAny()
      .orElseThrow(AuthorizationException::new));
  }

  private boolean passwordsMatch(String user, String passphrase, ApiKeyWithPassphrase key) {
    boolean result = passwordService.passwordsMatch(passphrase, key.getPassphrase());
    if (!result) {
      // this can only happen with a forged api key, so it may be relevant enough to issue a warning
      LOG.warn("got invalid api key for user {} with key id {}", user, key.getId());
    }
    return result;
  }

  public Collection<ApiKey> getKeys(String user) {
    return store.getOptional(user)
      .map(ApiKeyCollection::getKeys)
      .map(Collection::stream)
      .orElse(Stream.empty())
      .map(ApiKey::new)
      .collect(toList());
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

  private <T> T doSynchronized(String user, boolean write, Supplier<T> callback) {
    final ReadWriteLock lockFactory = locks.get(user);
    Lock lock = write ? lockFactory.writeLock() : lockFactory.readLock();
    lock.lock();
    try {
      return callback.get();
    } finally {
      lock.unlock();
    }
  }

  @Subscribe
  public void cleanupForDeletedUser(UserEvent userEvent) {
    if (userEvent.getEventType() == HandlerEventType.DELETE) {
      store.remove(userEvent.getItem().getId());
    }
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
