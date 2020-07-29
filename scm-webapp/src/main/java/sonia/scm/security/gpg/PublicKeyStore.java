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

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ContextEntry;
import sonia.scm.event.ScmEventBus;
import sonia.scm.security.NotPublicKeyException;
import sonia.scm.security.PublicKeyDeletedEvent;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static sonia.scm.security.gpg.PgpPublicKeyExtractor.getFromRawKey;

@Singleton
public class PublicKeyStore {

  private static final Logger LOG = LoggerFactory.getLogger(PublicKeyStore.class);

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
    UserPermissions.changePublicKeys(username).check();

    if (!rawKey.contains("PUBLIC KEY")) {
      throw new NotPublicKeyException(ContextEntry.ContextBuilder.entity(RawGpgKey.class, displayName).build(), "The provided key is not a public key");
    }

    Keys keys = Keys.resolve(rawKey);
    String master = keys.getMaster();

    for (String subKey : keys.getSubs()) {
      subKeyStore.put(subKey, new MasterKeyReference(master));
    }

    RawGpgKey key = new RawGpgKey(master, displayName, username, rawKey, getContactsFromPublicKey(rawKey), Instant.now());

    store.put(master, key);

    return key;

  }

  private Set<String> getContactsFromPublicKey(String rawKey) {
    Set<String> contacts = new HashSet<>();
    Optional<PGPPublicKey> publicKeyFromRawKey = getFromRawKey(rawKey);
    publicKeyFromRawKey.ifPresent(pgpPublicKey -> pgpPublicKey.getUserIDs().forEachRemaining(contacts::add));
    return contacts;
  }

  public void delete(String id) {
    RawGpgKey rawGpgKey = store.get(id);
    if (rawGpgKey != null) {
      UserPermissions.modify(rawGpgKey.getOwner()).check();
      store.remove(id);
      eventBus.post(new PublicKeyDeletedEvent());
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
