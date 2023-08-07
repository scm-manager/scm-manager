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

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.AlreadyExistsException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NoChangesMadeException;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.repository.Person;
import sonia.scm.repository.api.FileLock;
import sonia.scm.repository.api.FileLockedException;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.repository.work.WorkingCopy;
import sonia.scm.user.User;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SvnModifyCommandTest extends AbstractSvnCommandTestBase {

  private SvnModifyCommand svnModifyCommand;
  private SvnContext context;
  private SimpleSvnWorkingCopyFactory workingCopyFactory;

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-svn-spi-modify-test.zip";
  }

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Before
  public void initSvnModifyCommand() {
    context = createContext();
    workingCopyFactory = new SimpleSvnWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider(context.getDirectory(), repositoryLocationResolver, false)), new SimpleMeterRegistry());
    svnModifyCommand = new SvnModifyCommand(context, workingCopyFactory);
  }

  @Before
  public void initSecurityManager() {
    Subject subject = mock(Subject.class);
    PrincipalCollection principalCollection = mock(PrincipalCollection.class);
    when(subject.getPrincipal()).thenReturn("alThor");
    when(subject.getPrincipals()).thenReturn(principalCollection);
    when(principalCollection.oneByType(User.class)).thenReturn(new User("galaxy", "quest", "galaxy@quest.com"));
    ThreadContext.bind(subject);
  }

  @After
  public void cleanUpSecurityManager() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldRemoveFiles() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt", false));

    svnModifyCommand.execute(request);
    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/a.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/c")).exists();
  }

  @Test
  public void shouldRemoveDirectory() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("c", true));

    svnModifyCommand.execute(request);
    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/a.txt")).exists();
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/c")).doesNotExist();
  }

  @Test
  public void shouldAddNewFile() throws IOException {
    File testfile = temporaryFolder.newFile("Test123");

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Test123", testfile, false));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "Test123")).exists();
  }

  @Test
  public void shouldThrowNoChangesMadeExceptionIfEmptyCommit() throws IOException {
    File testfile = temporaryFolder.newFile("Test123");

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.ModifyFileRequest("g/h/j.txt", testfile));

    assertThrows(NoChangesMadeException.class, () -> svnModifyCommand.execute(request));
  }

  @Test
  public void shouldAddNewFileInDefaultPath() throws IOException {
    File testfile = temporaryFolder.newFile("Test123");

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.setDefaultPath(true);
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Test123", testfile, false));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "trunk/Test123")).exists();
  }

  @Test
  public void shouldThrowFileAlreadyExistsException() throws IOException {
    File testfile = temporaryFolder.newFile("a.txt");

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", testfile, false));

    assertThrows(AlreadyExistsException.class, () -> svnModifyCommand.execute(request));
  }

  @Test
  public void shouldUpdateExistingFile() throws IOException {
    File testfile = temporaryFolder.newFile("a.txt");

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", testfile, true));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "a.txt")).exists();
    assertThat(new File(workingCopy.getWorkingRepository(), "a.txt")).hasContent("");
  }

  @Test
  public void shouldThrowExceptionIfExpectedRevisionDoesNotMatch() throws IOException {
    File testfile = temporaryFolder.newFile("Test123");

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Test123", testfile, false));
    request.setExpectedRevision("42");

    assertThrows(ConcurrentModificationException.class, () -> svnModifyCommand.execute(request));

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "Test123")).doesNotExist();
  }

  @Test
  @SuppressWarnings("java:S2699") // we just want to ensure that no exception is thrown
  public void shouldPassIfExpectedRevisionMatchesCurrentRevision() throws IOException {
    File testfile = temporaryFolder.newFile("Test123");

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Test123", testfile, false));
    request.setExpectedRevision("6");

    svnModifyCommand.execute(request);

    // nothing to check here; we just want to ensure that no exception is thrown
  }

  @Test(expected = ScmConstraintViolationException.class)
  public void shouldThrowErrorIfRelativePathIsOutsideOfWorkdir() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/../../../../b.txt", false));

    svnModifyCommand.execute(request);
  }

  @Test
  public void shouldRenameFile() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/b.txt", false));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "a.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository(), "b.txt")).exists();
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldThrowAlreadyExistsException() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/c", false));

    svnModifyCommand.execute(request);
  }

  @Test
  public void shouldRenameFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "/notc", false));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "c/d.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository(), "c/e.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository(), "notc/d.txt")).exists();
    assertThat(new File(workingCopy.getWorkingRepository(), "notc/e.txt")).exists();
  }

  @Test
  public void shouldMoveFileToExistingFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/c/z.txt", false));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "a.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository(), "c/z.txt")).exists();
    assertThat(new File(workingCopy.getWorkingRepository(), "c/d.txt")).exists();
    assertThat(new File(workingCopy.getWorkingRepository(), "c/e.txt")).exists();
  }

  @Test
  public void shouldMoveFolderToExistingFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("g/h", "/h/h", false));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "g/h/j.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository(), "h/h/j.txt")).exists();
  }

  @Test
  public void shouldMoveFileToNonExistentFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/y/z.txt", false));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "a.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository(), "y/z.txt")).exists();
  }

  @Test
  public void shouldMoveFileWithOverwrite() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "b.txt", true));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "a.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository(), "b.txt")).exists();
  }

  @Test
  public void shouldMoveFolderToNonExistentFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "/j/k/c", false));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "c/d.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository(), "c/e.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository(), "j/k/c/d.txt")).exists();
    assertThat(new File(workingCopy.getWorkingRepository(), "j/k/c/e.txt")).exists();
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldFailMoveAndKeepFilesWhenSourceAndTargetAreTheSame() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "c", false));

    svnModifyCommand.execute(request);
  }

  @Test
  public void shouldFailIfLockedByOtherPerson() {
    Subject subject = mock(Subject.class);
    when(subject.getPrincipal()).thenReturn("Perrin");
    ThreadContext.bind(subject);

    lockFile();

    initSecurityManager();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt", false));

    assertThrows(FileLockedException.class, () -> svnModifyCommand.execute(request));
    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/a.txt")).exists();
  }

  @Test
  public void shouldSucceedIfLockedByUser() {
    lockFile();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt", false));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/a.txt")).doesNotExist();
    assertThat(getLock()).isEmpty();
  }

  private ModifyCommandRequest prepareModifyCommandRequest() {
    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("modify some things");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));
    return request;
  }

  private void lockFile() {
    SvnFileLockCommand svnFileLockCommand = new SvnFileLockCommand(context);
    LockCommandRequest lockRequest = new LockCommandRequest();
    lockRequest.setFile("a.txt");
    svnFileLockCommand.lock(lockRequest);
  }

  private Optional<FileLock> getLock() {
    SvnFileLockCommand svnFileLockCommand = new SvnFileLockCommand(context);
    LockStatusCommandRequest request = new LockStatusCommandRequest();
    request.setFile("a.txt");
    return svnFileLockCommand.status(request);
  }
}
