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

import org.assertj.core.api.AbstractObjectAssert;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.FileLock;
import sonia.scm.store.DataStoreFactory;
import sonia.scm.store.InMemoryByteDataStoreFactory;

import java.time.Clock;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GitLockStoreFactoryTest {

  private static final Instant NOW = Instant.ofEpochSecond(-562031958);

  private final DataStoreFactory dataStoreFactory = new InMemoryByteDataStoreFactory();
  private final Clock clock = mock(Clock.class);
  private String currentUser = "dent";
  private final GitLockStoreFactory gitLockStoreFactory = new GitLockStoreFactory(dataStoreFactory, clock, () -> currentUser);

  private final Repository repository = RepositoryTestData.createHeartOfGold();

  @BeforeEach
  void setClock() {
    when(clock.instant()).thenReturn(NOW);
  }

  @Test
  void shouldHaveNoLockOnStartup() {
    GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);

    assertThat(gitLockStore.getLock("some/file.txt"))
      .isEmpty();
  }

  @Test
  void shouldNotFailOnRemovingNonExistingLock() {
    GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);

    gitLockStore.remove("some/file.txt", false);

    assertThat(gitLockStore.getLock("some/file.txt"))
      .isEmpty();
  }

  @Test
  void shouldStoreAndRetrieveLock() {
    GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);

    gitLockStore.put("some/file.txt", false);

    AbstractObjectAssert<?, FileLock> lockAssert = assertThat(gitLockStore.getLock("some/file.txt"))
      .get();
    lockAssert
      .extracting("userId")
      .isEqualTo("dent");
    lockAssert
      .extracting("timestamp")
      .isEqualTo(NOW);
  }

  @Test
  void shouldRemoveLock() {
    GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);
    gitLockStore.put("some/file.txt", false);

    gitLockStore.remove("some/file.txt", false);

    assertThat(gitLockStore.getLock("some/file.txt"))
      .isEmpty();
  }

  @Nested
  class WithExistingLockFromOtherUser {

    @BeforeEach
    void setLock() {
      currentUser = "trillian";
      GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);
      gitLockStore.put("some/file.txt", false);
      currentUser = "dent";
    }

    @Test
    void shouldNotRemoveExistingLockWithoutForce() {
      GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);

      assertThrows(FileLockedException.class, () -> gitLockStore.remove("some/file.txt", false));

      assertThat(gitLockStore.getLock("some/file.txt"))
        .get()
        .extracting("userId")
        .isEqualTo("trillian");
    }

    @Test
    void shouldRemoveExistingLockWithForce() {
      GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);

      gitLockStore.remove("some/file.txt", true);

      assertThat(gitLockStore.getLock("some/file.txt"))
        .isEmpty();
    }

    @Test
    void shouldNotOverrideExistingLockWithoutForce() {
      GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);

      assertThrows(FileLockedException.class, () -> gitLockStore.put("some/file.txt", false));

      assertThat(gitLockStore.getLock("some/file.txt"))
        .get()
        .extracting("userId")
        .isEqualTo("trillian");
    }

    @Test
    void shouldOverrideExistingLockWithForce() {
      GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);

      gitLockStore.put("some/file.txt", true);

      assertThat(gitLockStore.getLock("some/file.txt"))
        .get()
        .extracting("userId")
        .isEqualTo("dent");
    }
  }

  @Test
  void shouldFailSettingLockTwiceAndKeepExistingLockWithoutForce() {
    GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);
    gitLockStore.put("some/file.txt", false);

    assertThrows(FileLockedException.class, () -> gitLockStore.put("some/file.txt", false));

    assertThat(gitLockStore.getLock("some/file.txt"))
      .get()
      .extracting("timestamp")
      .isEqualTo(NOW);
  }

  @Test
  void shouldOverrideExistingLockWithForce() {
    GitLockStoreFactory.GitLockStore gitLockStore = gitLockStoreFactory.create(repository);
    gitLockStore.put("some/file.txt", false);

    Instant THEN = NOW.plus(42, SECONDS);
    when(clock.instant()).thenReturn(THEN);
    gitLockStore.put("some/file.txt", true);

    assertThat(gitLockStore.getLock("some/file.txt"))
      .get()
      .extracting("timestamp")
      .isEqualTo(THEN);
  }
}
