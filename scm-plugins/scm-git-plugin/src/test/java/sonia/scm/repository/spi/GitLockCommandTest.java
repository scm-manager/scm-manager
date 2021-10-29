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
import sonia.scm.repository.spi.GitLockStoreFactory.GitLockStore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitLockCommandTest {

  private static final Repository REPOSITORY = RepositoryTestData.createHeartOfGold();
  private static final Instant NOW = Instant.ofEpochSecond(-562031958);

  @Mock
  private GitContext context;
  @Mock
  private GitLockStoreFactory lockStoreFactory;
  @Mock
  private GitLockStore lockStore;

  @InjectMocks
  private GitLockCommand lockCommand;

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
