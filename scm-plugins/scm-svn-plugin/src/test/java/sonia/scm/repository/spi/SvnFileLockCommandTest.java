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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.io.SVNRepository;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.FileLockedException;
import sonia.scm.repository.api.LockCommandResult;
import sonia.scm.repository.api.UnlockCommandResult;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SvnFileLockCommandTest extends AbstractSvnCommandTestBase {

  @Before
  public void mockDefaultSubject() {
    mockSubject("trillian");
  }

  private void mockSubject(String name) {
    Subject subject = mock(Subject.class);
    ThreadContext.bind(subject);
    when(subject.getPrincipal()).thenReturn(name);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldLockFile() throws SVNException {
    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());

    LockCommandRequest request = new LockCommandRequest();
    request.setFile("a.txt");

    LockCommandResult lockResult = lockCommand.lock(request);

    assertThat(lockResult.isSuccessful()).isTrue();
    assertSingleLock("a.txt", "trillian");
  }

  @Test
  public void shouldUnlockFile() throws SVNException {
    createLock("a.txt");

    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());
    UnlockCommandRequest request = new UnlockCommandRequest();
    request.setFile("a.txt");

    UnlockCommandResult unlockResult = lockCommand.unlock(request);

    assertThat(unlockResult.isSuccessful()).isTrue();
    assertThat(getLocks("a.txt")).isEmpty();
  }

  @Test
  public void shouldUnlockFileOnlyIfPredicateMatches() throws SVNException {
    createLock("a.txt");

    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());
    UnlockCommandRequest request = new UnlockCommandRequest();
    request.setFile("a.txt");

    assertThrows(FileLockedException.class, () -> lockCommand.unlock(request, lock -> false));
    assertThat(getLocks("a.txt")).isNotEmpty();

    lockCommand.unlock(request, lock -> true);
    assertThat(getLocks("a.txt")).isEmpty();
  }

  @Test
  public void shouldNotFailUnlockingNotLockedFile() throws SVNException {
    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());
    UnlockCommandRequest request = new UnlockCommandRequest();
    request.setFile("a.txt");

    UnlockCommandResult unlockResult = lockCommand.unlock(request);

    assertThat(unlockResult.isSuccessful()).isTrue();
    assertThat(getLocks("a.txt")).isEmpty();
  }

  @Test
  public void shouldFailToUnlockFileLockedByOtherUser() throws SVNException {
    createLockFromOtherUser();

    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());
    UnlockCommandRequest request = new UnlockCommandRequest();
    request.setFile("a.txt");

    assertThrows(
      "File a.txt locked by dent.",
      FileLockedException.class,
      () -> lockCommand.unlock(request));
    assertThat(getLocks("a.txt")).isNotEmpty();
  }

  @Test
  public void shouldUnlockFileLockedByOtherUserWithForce() throws SVNException {
    createLockFromOtherUser();

    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());
    UnlockCommandRequest request = new UnlockCommandRequest();
    request.setFile("a.txt");
    request.setForce(true);

    UnlockCommandResult unlockResult = lockCommand.unlock(request);

    assertThat(unlockResult.isSuccessful()).isTrue();
    assertThat(getLocks("a.txt")).isEmpty();
  }

  @Test
  public void shouldNotOverwriteLockFromOtherUser() {
    createLockFromOtherUser();

    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());

    LockCommandRequest request = new LockCommandRequest();
    request.setFile("a.txt");

    assertThrows(
      "File a.txt locked by dent.",
      FileLockedException.class,
      () -> lockCommand.lock(request));
  }

  @Test
  public void shouldNotRemoveLockFromOtherUserEvenWithPredicate() {
    createLockFromOtherUser();

    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());

    UnlockCommandRequest request = new UnlockCommandRequest();
    request.setFile("a.txt");

    assertThrows(
      "File a.txt locked by dent.",
      FileLockedException.class,
      () -> lockCommand.unlock(request, lock -> true));
  }

  @Test
  public void shouldGetEmptyLocksWithoutLocks() {
    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());

    Collection<FileLock> locks = lockCommand.getAll();

    assertThat(locks).isEmpty();
  }

  @Test
  public void shouldGetLocks() {
    createLock("a.txt");
    createLock("c/e.txt");

    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());

    Collection<FileLock> locks = lockCommand.getAll();

    assertThat(locks)
      .hasSize(2)
      .extracting("path")
      .containsExactly("a.txt", "c/e.txt");
  }

  @Test
  public void shouldGetNoStatusForUnlockedFile() {
    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());

    LockStatusCommandRequest lockStatusCommandRequest = new LockStatusCommandRequest();
    lockStatusCommandRequest.setFile("a.txt");
    Optional<FileLock> status = lockCommand.status(lockStatusCommandRequest);

    assertThat(status).isEmpty();
  }

  @Test
  public void shouldGetStatusForLockedFile() {
    createLock("a.txt");

    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());

    LockStatusCommandRequest lockStatusCommandRequest = new LockStatusCommandRequest();
    lockStatusCommandRequest.setFile("a.txt");
    Optional<FileLock> status = lockCommand.status(lockStatusCommandRequest);

    assertThat(status)
      .get()
      .extracting("userId")
      .isEqualTo("trillian");
  }

  private void createLockFromOtherUser() {
    mockSubject("dent");
    createLock("a.txt");
    mockDefaultSubject();
  }

  private void createLock(String path) {
    SvnFileLockCommand lockCommand = new SvnFileLockCommand(createContext());
    LockCommandRequest request = new LockCommandRequest();
    request.setFile(path);
    lockCommand.lock(request);
  }

  private void assertSingleLock(String path, String user) throws SVNException {
    SVNLock[] locks = getLocks(path);
    assertThat(locks)
      .extracting("path")
      .containsExactly("/" +
        path);
    assertThat(locks)
      .extracting("owner")
      .containsExactly(user);
  }

  private SVNLock[] getLocks(String path) throws SVNException {
    SVNRepository svnRepository = createContext().open();
    return svnRepository.getLocks(path);
  }
}
