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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.LockCommandResult;
import sonia.scm.repository.api.UnlockCommandResult;
import sonia.scm.repository.spi.GitFileLockStoreFactory.GitFileLockStore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitFileLockCommandTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private static final Instant NOW = Instant.ofEpochSecond(-562031958);

  @Mock
  private GitContext context;
  @Mock
  private GitFileLockStoreFactory lockStoreFactory;
  @Mock
  private GitFileLockStore lockStore;

  @InjectMocks
  private GitFileLockCommand lockCommand;

  @BeforeEach
  void initContext() {
    when(context.getRepository()).thenReturn(REPOSITORY);
  }

  @BeforeEach
  void initStoreFactory() {
    when(lockStoreFactory.create(REPOSITORY)).thenReturn(lockStore);
  }

  @Test
  void shouldSetLockOnLockRequest() {
    LockCommandRequest request = new LockCommandRequest();
    request.setFile("some/file.txt");

    LockCommandResult lock = lockCommand.lock(request);

    assertThat(lock.isSuccessful()).isTrue();
    verify(lockStore).put("some/file.txt");
  }

  @Test
  void shouldUnlockOnUnlockRequest() {
    UnlockCommandRequest request = new UnlockCommandRequest();
    request.setFile("some/file.txt");

    UnlockCommandResult lock = lockCommand.unlock(request);

    assertThat(lock.isSuccessful()).isTrue();
    verify(lockStore).remove("some/file.txt", false);
  }

  @Test
  void shouldUnlockWithForceOnUnlockRequestWithForce() {
    UnlockCommandRequest request = new UnlockCommandRequest();
    request.setFile("some/file.txt");
    request.setForce(true);

    UnlockCommandResult lock = lockCommand.unlock(request);

    assertThat(lock.isSuccessful()).isTrue();
    verify(lockStore).remove("some/file.txt", true);
  }

  @Test
  void shouldGetStatus() {
    when(lockStore.getLock("some/file.txt"))
      .thenReturn(of(new FileLock("some/file.txt", "42", "dent", NOW)));

    LockStatusCommandRequest request = new LockStatusCommandRequest();
    request.setFile("some/file.txt");

    Optional<FileLock> status = lockCommand.status(request);

    AbstractObjectAssert<?, FileLock> statusAssert = assertThat(status).get();
    statusAssert
      .extracting("id")
      .isEqualTo("42");
    statusAssert
      .extracting("path")
      .isEqualTo("some/file.txt");
    statusAssert
      .extracting("userId")
      .isEqualTo("dent");
    statusAssert
      .extracting("timestamp")
      .isEqualTo(NOW);
  }

  @Test
  void shouldGetAll() {
    ArrayList<FileLock> existingLocks = new ArrayList<>();
    when(lockStore.getAll()).thenReturn(existingLocks);

    Collection<FileLock> all = lockCommand.getAll();

    assertThat(all).isSameAs(existingLocks);
  }
}
