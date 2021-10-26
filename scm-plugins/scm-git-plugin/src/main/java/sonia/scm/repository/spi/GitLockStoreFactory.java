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

package sonia.scm.repository.spi;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.shiro.SecurityUtils;
import sonia.scm.repository.Repository;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.FileLockedException;
import sonia.scm.store.DataStore;
import sonia.scm.store.DataStoreFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.bind.annotation.XmlRootElement;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Singleton
public final class GitLockStoreFactory {

  private static final String STORE_ID = "locks";

  private final DataStoreFactory dataStoreFactory;
  private final Clock clock;
  private final Supplier<String> currentUser;

  @Inject
  public GitLockStoreFactory(DataStoreFactory dataStoreFactory) {
    this(dataStoreFactory, Clock.systemDefaultZone(), () -> SecurityUtils.getSubject().getPrincipals().oneByType(String.class));
  }

  GitLockStoreFactory(DataStoreFactory dataStoreFactory, Clock clock, Supplier<String> currentUser) {
    this.dataStoreFactory = dataStoreFactory;
    this.clock = clock;
    this.currentUser = currentUser;
  }

  public GitLockStore create(Repository repository) {
    return new GitLockStore(repository);
  }

  public final class GitLockStore {

    private final Repository repository;
    private final DataStore<StoreEntry> store;

    public GitLockStore(Repository repository) {
      this.repository = repository;
      this.store =
        dataStoreFactory
          .withType(StoreEntry.class)
          .withName("file-locks")
          .forRepository(repository)
          .build();
    }

    void put(String file, boolean force) {
      StoreEntry storeEntry = readEntry();
      if (!force && storeEntry.get(file).isPresent()) {
        throw new FileLockedException(repository.getNamespaceAndName(), file);
      }
      storeEntry.add(file, new FileLock(currentUser.get(), Instant.now(clock)));
      store(storeEntry);
    }

    void remove(String file, boolean force) {
      StoreEntry storeEntry = readEntry();
      Optional<FileLock> existingFileLock = storeEntry.get(file);
      if (existingFileLock.isPresent()) {
        if (!force && !currentUser.get().equals(existingFileLock.get().getUserId())) {
          throw new FileLockedException(repository.getNamespaceAndName(), file);
        }
        storeEntry.remove(file);
        store(storeEntry);
      }
    }

    Optional<FileLock> getLock(String file) {
      return readEntry().get(file);
    }

    private StoreEntry readEntry() {
      return store.getOptional(STORE_ID).orElse(new StoreEntry());
    }

    private void store(StoreEntry storeEntry) {
      store.put(STORE_ID, storeEntry);
    }
  }

  @Data
  @XmlRootElement(name = "file-locks")
  private static class StoreEntry {
    private Map<String, StoredFileLock> files;

    Optional<FileLock> get(String file) {
      if (files == null) {
        return empty();
      }
      return ofNullable(files.get(file)).map(StoredFileLock::toFileLock);
    }

    void add(String file, FileLock lock) {
      if (files == null) {
        files = new HashMap<>();
      }
      files.put(file, new StoredFileLock(lock));
    }

    void remove(String file) {
      if (files == null) {
        return;
      }
      files.remove(file);
    }
  }

  @Data
  @NoArgsConstructor
  private static class StoredFileLock {
    private String userId;
    private long timestamp;

    StoredFileLock(FileLock fileLock) {
      this.userId = fileLock.getUserId();
      this.timestamp = fileLock.getTimestamp().getEpochSecond();
    }

    FileLock toFileLock() {
      return new FileLock(userId, Instant.ofEpochSecond(timestamp));
    }
  }
}
