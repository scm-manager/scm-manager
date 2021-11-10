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
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.AlreadyExistsException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.repository.Person;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.repository.work.WorkingCopy;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SvnModifyCommandTest extends AbstractSvnCommandTestBase {

  private SvnModifyCommand svnModifyCommand;
  private SvnContext context;
  private SimpleSvnWorkingCopyFactory workingCopyFactory;

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
    when(subject.getPrincipal()).thenReturn("alThor");
    ThreadContext.bind(subject);
  }

  @After
  public void cleanUpSecurityManager() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldRemoveFiles() {
    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt", false));
    request.setCommitMessage("this is great");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));

    svnModifyCommand.execute(request);
    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/a.txt")).doesNotExist();
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/c")).exists();
  }

  @Test
  public void shouldRemoveDirectory() {
    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("c", true));
    request.setCommitMessage("this is great");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));

    svnModifyCommand.execute(request);
    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/a.txt")).exists();
    assertThat(new File(workingCopy.getWorkingRepository().getAbsolutePath() + "/c")).doesNotExist();
  }

  @Test
  public void shouldAddNewFile() throws IOException {
    File testfile = temporaryFolder.newFile("Test123");

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Test123", testfile, false));
    request.setCommitMessage("this is great");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "Test123")).exists();
  }

  @Test
  public void shouldAddNewFileInDefaultPath() throws IOException {
    File testfile = temporaryFolder.newFile("Test123");

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setDefaultPath(true);
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Test123", testfile, false));
    request.setCommitMessage("this is great");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "trunk/Test123")).exists();
  }

  @Test
  public void shouldThrowFileAlreadyExistsException() throws IOException {
    File testfile = temporaryFolder.newFile("a.txt");

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", testfile, false));
    request.setCommitMessage("this is great");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));

    assertThrows(AlreadyExistsException.class, () -> svnModifyCommand.execute(request));
  }

  @Test
  public void shouldUpdateExistingFile() throws IOException {
    File testfile = temporaryFolder.newFile("a.txt");

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", testfile, true));
    request.setCommitMessage("this is great");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));

    svnModifyCommand.execute(request);

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "a.txt")).exists();
    assertThat(new File(workingCopy.getWorkingRepository(), "a.txt")).hasContent("");
  }

  @Test
  public void shouldThrowExceptionIfExpectedRevisionDoesNotMatch() throws IOException {
    File testfile = temporaryFolder.newFile("Test123");

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Test123", testfile, false));
    request.setCommitMessage("this should not pass");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));
    request.setExpectedRevision("42");

    assertThrows(ConcurrentModificationException.class, () -> svnModifyCommand.execute(request));

    WorkingCopy<File, File> workingCopy = workingCopyFactory.createWorkingCopy(context, null);
    assertThat(new File(workingCopy.getWorkingRepository(), "Test123")).doesNotExist();
  }

  @Test
  @SuppressWarnings("java:S2699") // we just want to ensure that no exception is thrown
  public void shouldPassIfExpectedRevisionMatchesCurrentRevision() throws IOException {
    File testfile = temporaryFolder.newFile("Test123");

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Test123", testfile, false));
    request.setCommitMessage("this should not pass");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));
    request.setExpectedRevision("5");

    svnModifyCommand.execute(request);

    // nothing to check here; we just want to ensure that no exception is thrown
  }
}
