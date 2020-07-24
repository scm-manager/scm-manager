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

import org.bouncycastle.openpgp.PGPException;
import sonia.scm.ContextEntry;
import sonia.scm.security.NotPublicKeyException;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.user.UserPermissions;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Singleton
public class PublicKeyStore {

  private static final String STORE_NAME = "gpg_public_keys";

  private final DataStore<RawGpgKey> store;

  @Inject
  public PublicKeyStore(DataStoreFactory dataStoreFactory) {
    this.store = dataStoreFactory.withType(RawGpgKey.class).withName(STORE_NAME).build();
  }

  public RawGpgKey add(String displayName, String username, String rawKey) {
    UserPermissions.modify(username).check();

    if (!rawKey.contains("PUBLIC KEY")) {
      throw new NotPublicKeyException(ContextEntry.ContextBuilder.entity(RawGpgKey.class, displayName).build(), "The provided key is not a public key");
    }

    try {
      String id = Keys.resolveIdFromKey(rawKey);
      RawGpgKey key = new RawGpgKey(id, displayName, username, rawKey, Instant.now());

      store.put(id, key);

      return key;
    } catch (IOException | PGPException e) {
      throw new GPGException("failed to resolve id from gpg key");
    }
  }

  public void delete(String id) {
    RawGpgKey rawGpgKey = store.get(id);
    if (rawGpgKey != null) {
      UserPermissions.modify(rawGpgKey.getOwner()).check();
      store.remove(id);
    }
  }

  public Optional<RawGpgKey> findById(String id) {
    return store.getOptional(id);
  }

  public List<RawGpgKey> findByUsername(String username) {
    return store.getAll().values()
      .stream()
      .filter(rawGpgKey -> username.equalsIgnoreCase(rawGpgKey.getOwner()))
      .collect(Collectors.toList());
  }

}
