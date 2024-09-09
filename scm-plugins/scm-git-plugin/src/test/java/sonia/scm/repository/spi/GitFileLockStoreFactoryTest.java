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

package sonia.scm.repository.spi;

import org.assertj.core.api.AbstractObjectAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.FileLockedException;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryByteDataStoreFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitFileLockStoreFactoryTest {

  private static final Instant NOW = Instant.ofEpochSecond(-562031958);

  private final DataStoreFactory dataStoreFactory = new InMemoryByteDataStoreFactory();
  private final Clock clock = mock(Clock.class);
  private String currentUser = "dent";
  private int nextId = 0;
  private final GitFileLockStoreFactory gitFileLockStoreFactory =
    new GitFileLockStoreFactory(dataStoreFactory, () -> "id-" + (nextId++), clock, () -> currentUser);

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  void setClock() {
    when(clock.instant()).thenReturn(NOW);
  }

  @Test
  void shouldHaveNoLockOnStartup() {
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

    assertThat(gitFileLockStore.getLock("some/file.txt"))
      .isEmpty();
    assertThat(gitFileLockStore.getAll())
      .isEmpty();
    assertThat(gitFileLockStore.hasLocks())
      .isFalse();
  }

  @Test
  void shouldNotFailOnRemovingNonExistingLock() {
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

    gitFileLockStore.remove("some/file.txt", false);

    assertThat(gitFileLockStore.getLock("some/file.txt"))
      .isEmpty();
  }

  @Test
  void shouldStoreAndRetrieveLock() {
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

    FileLock createdLock = gitFileLockStore.put("some/file.txt");

    Optional<FileLock> retrievedLock = gitFileLockStore.getLock("some/file.txt");

    AbstractObjectAssert<?, FileLock> lockAssert = assertThat(retrievedLock)
      .get();
    lockAssert
      .extracting("userId")
      .isEqualTo("dent");
    lockAssert
      .extracting("id")
      .isEqualTo("id-0");
    lockAssert
      .extracting("timestamp")
      .isEqualTo(NOW);
    lockAssert
      .usingRecursiveComparison()
      .isEqualTo(createdLock);

    assertThat(gitFileLockStore.getAll())
      .extracting("userId")
      .containsExactly("dent");
  }

  @Test
  void shouldRetrieveLockById() {
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

    FileLock createdLock = gitFileLockStore.put("some/file.txt");

    Optional<FileLock> retrievedLock = gitFileLockStore.getById(createdLock.getId());

    assertThat(retrievedLock)
      .get()
      .usingRecursiveComparison()
      .isEqualTo(createdLock);
  }

  @Test
  void shouldHaveLock() {
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

    gitFileLockStore.put("some/file.txt");

    assertThat(gitFileLockStore.hasLocks())
      .isTrue();
  }

  @Test
  void shouldBeModifiableWithoutLock() {
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

    gitFileLockStore.assertModifiable("some/file.txt");
  }

  @Test
  void shouldBeModifiableWithOwnLock() {
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);
    gitFileLockStore.put("some/file.txt");

    gitFileLockStore.assertModifiable("some/file.txt");
  }

  @Test
  void shouldRemoveLock() {
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);
    FileLock createdLock = gitFileLockStore.put("some/file.txt");

    gitFileLockStore.remove("some/file.txt", false);

    assertThat(gitFileLockStore.getLock("some/file.txt"))
      .isEmpty();
    assertThat(gitFileLockStore.getById(createdLock.getId()))
      .isEmpty();
  }

  @Test
  void shouldRemoveLockById() {
    GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);
    FileLock createdLock = gitFileLockStore.put("some/file.txt");

    gitFileLockStore.removeById(createdLock.getId(), false);

    assertThat(gitFileLockStore.getLock("some/file.txt"))
      .isEmpty();
    assertThat(gitFileLockStore.getById(createdLock.getId()))
      .isEmpty();
  }

  @Nested
  class WithExistingLockFromOtherUser {

    @BeforeEach
    void setLock() {
      currentUser = "trillian";
      GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);
      gitFileLockStore.put("some/file.txt");
      currentUser = "dent";
    }

    @Test
    void shouldNotRemoveExistingLockWithoutForce() {
      GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

      assertThrows(FileLockedException.class, () -> gitFileLockStore.remove("some/file.txt", false));

      assertThat(gitFileLockStore.getLock("some/file.txt"))
        .get()
        .extracting("userId")
        .isEqualTo("trillian");
    }

    @Test
    void shouldRemoveExistingLockWithForce() {
      GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

      gitFileLockStore.remove("some/file.txt", true);

      assertThat(gitFileLockStore.getLock("some/file.txt"))
        .isEmpty();
    }

    @Test
    void shouldNotBeModifiableWithOwnLock() {
      GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

      assertThrows(FileLockedException.class, () -> gitFileLockStore.assertModifiable("some/file.txt"));
    }

    @Test
    void shouldNotOverrideExistingLock() {
      GitFileLockStoreFactory.GitFileLockStore gitFileLockStore = gitFileLockStoreFactory.create(repository);

      assertThrows(FileLockedException.class, () -> gitFileLockStore.put("some/file.txt"));

      assertThat(gitFileLockStore.getLock("some/file.txt"))
        .get()
        .extracting("userId")
        .isEqualTo("trillian");
    }
  }
}
