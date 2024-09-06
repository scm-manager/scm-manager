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

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.bouncycastle.openpgp.PGPPublicKey;
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Person;
import sonia.scm.security.NotPublicKeyException;
import sonia.scm.security.PublicKeyCreatedEvent;
import sonia.scm.security.PublicKeyDeletedEvent;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.user.UserPermissions;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static sonia.scm.security.gpg.KeysExtractor.extractPublicKey;

@Singleton
public class PublicKeyStore {

  private static final String STORE_NAME = "gpg_public_keys";
  private static final String SUBKEY_STORE_NAME = "gpg_public_sub_keys";

  private final DataStore<RawGpgKey> store;
  private final DataStore<MasterKeyReference> subKeyStore;
  private final ScmEventBus eventBus;

  @Inject
  public PublicKeyStore(DataStoreFactory dataStoreFactory, ScmEventBus eventBus) {
    this.store = dataStoreFactory.withType(RawGpgKey.class).withName(STORE_NAME).build();
    this.subKeyStore = dataStoreFactory.withType(MasterKeyReference.class).withName(SUBKEY_STORE_NAME).build();
    this.eventBus = eventBus;
  }

  public RawGpgKey add(String displayName, String username, String rawKey) {
    return add(displayName, username, rawKey, false);
  }

  public RawGpgKey add(String displayName, String username, String rawKey, boolean readonly) {
    UserPermissions.changePublicKeys(username).check();

    if (!rawKey.contains("PUBLIC KEY")) {
      throw new NotPublicKeyException(
        ContextEntry.ContextBuilder.entity(RawGpgKey.class, displayName).build(),
        "The provided key is not a public key"
      );
    }

    preventOverwriteReadOnlyKeys(rawKey);

    Keys keys = Keys.resolve(rawKey);
    String master = keys.getMaster();

    for (String subKey : keys.getSubs()) {
      subKeyStore.put(subKey, new MasterKeyReference(master));
    }

    RawGpgKey key = new RawGpgKey(master, displayName, username, rawKey, getContactsFromPublicKey(rawKey), Instant.now(), readonly);

    store.put(master, key);
    eventBus.post(new PublicKeyCreatedEvent(RawGpgKeyToDefaultPublicKeyMapper.map(key)));

    return key;

  }

  private void preventOverwriteReadOnlyKeys(String rawKey) {
    Optional<RawGpgKey> existingReadOnlyKey = store.getAll().values()
      .stream()
      .filter(k -> k.getRaw().trim().equals(rawKey.trim()))
      .filter(RawGpgKey::isReadonly)
      .findFirst();
    if (existingReadOnlyKey.isPresent()) {
      throw new DeletingReadonlyKeyNotAllowedException(existingReadOnlyKey.get().getId());
    }
  }

  private Set<Person> getContactsFromPublicKey(String rawKey) {
    List<String> userIds = new ArrayList<>();
    PGPPublicKey publicKeyFromRawKey = extractPublicKey(rawKey);
    publicKeyFromRawKey.getUserIDs().forEachRemaining(userIds::add);

    return userIds.stream().map(Person::toPerson).collect(Collectors.toSet());
  }

  public void delete(String id) {
    RawGpgKey rawGpgKey = store.get(id);
    if (rawGpgKey != null) {
      if (!rawGpgKey.isReadonly()) {
        UserPermissions.changePublicKeys(rawGpgKey.getOwner()).check();
        store.remove(id);
        eventBus.post(new PublicKeyDeletedEvent(RawGpgKeyToDefaultPublicKeyMapper.map(rawGpgKey)));
      } else {
        throw new DeletingReadonlyKeyNotAllowedException(id);
      }
    }
  }

  public Optional<RawGpgKey> findById(String id) {
    Optional<MasterKeyReference> reference = subKeyStore.getOptional(id);

    if (reference.isPresent()) {
      return store.getOptional(reference.get().getMasterKey());
    }

    return store.getOptional(id);
  }

  public List<RawGpgKey> findByUsername(String username) {
    return store.getAll().values()
      .stream()
      .filter(rawGpgKey -> username.equalsIgnoreCase(rawGpgKey.getOwner()))
      .collect(Collectors.toList());
  }

}
