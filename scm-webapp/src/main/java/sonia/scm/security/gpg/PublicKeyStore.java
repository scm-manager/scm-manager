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

import com.google.common.annotations.VisibleForTesting;
import org.apache.shiro.SecurityUtils;
import org.bouncycastle.openpgp.PGPException;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

@Singleton
public class PublicKeyStore {

  private static final String STORE_NAME = "gpg_public_keys";

  private final DataStore<RawGpgKey> store;
  private final Supplier<String> currentUserSupplier;

  @Inject
  public PublicKeyStore(DataStoreFactory dataStoreFactory) {
    this(
      dataStoreFactory.withType(RawGpgKey.class).withName(STORE_NAME).build(),
      () -> SecurityUtils.getSubject().getPrincipal().toString()
    );
  }

  @VisibleForTesting
  PublicKeyStore(DataStore<RawGpgKey> store, Supplier<String> currentUserSupplier) {
    this.store = store;
    this.currentUserSupplier = currentUserSupplier;
  }

  public RawGpgKey add(String displayName, String rawKey) {
    try {
      String id = Keys.resolveIdFromKey(rawKey);
      RawGpgKey key = new RawGpgKey(id, displayName, currentUserSupplier.get(), rawKey, Instant.now());

      store.put(id, key);

      return key;
    } catch (IOException | PGPException e) {
      throw new GPGException("failed to resolve id from gpg key");
    }
  }

  public Optional<RawGpgKey> findById(String id) {
    return store.getOptional(id);
  }

}
