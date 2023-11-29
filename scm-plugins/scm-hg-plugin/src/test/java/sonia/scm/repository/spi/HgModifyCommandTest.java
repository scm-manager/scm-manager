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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.AlreadyExistsException;
import sonia.scm.NoChangesMadeException;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.repository.Person;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;

import static org.assertj.core.api.Assertions.assertThat;

public class HgModifyCommandTest extends AbstractHgCommandTestBase {

  private HgModifyCommand hgModifyCommand;

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-hg-spi-modify-test.zip";
  }

  @Before
  public void initHgModifyCommand() {
    SimpleHgWorkingCopyFactory workingCopyFactory = new SimpleHgWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider(null, repositoryLocationResolver)), new SimpleMeterRegistry()) {
      @Override
      public void configure(org.javahg.commands.PullCommand pullCommand) {
        // we do not want to configure http hooks in this unit test
      }
    };
    hgModifyCommand = new HgModifyCommand(cmdContext, workingCopyFactory);
  }

  @Test
  public void shouldRemoveFiles() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt", false));

    String result = hgModifyCommand.execute(request);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(result);
    assertThat(cmdContext.open().tip().getDeletedFiles().size()).isEqualTo(1);
  }

  @Test
  public void shouldRemoveDirectoriesRecursively() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("c", true));

    String result = hgModifyCommand.execute(request);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(result);
    assertThat(cmdContext.open().tip().getDeletedFiles().size()).isEqualTo(2);
  }

  @Test
  public void shouldCreateFilesWithoutOverwrite() throws IOException {
    File testFile = temporaryFolder.newFile();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));

    String changeSet = hgModifyCommand.execute(request);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(changeSet);
    assertThat(cmdContext.open().tip().getAddedFiles().size()).isEqualTo(1);
  }

  @Test
  public void shouldOverwriteExistingFiles() throws IOException {
    File testFile = temporaryFolder.newFile();

    try (FileOutputStream fos = new FileOutputStream(testFile)) {
      fos.write(42);
    }
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", testFile, true));

    String changeSet2 = hgModifyCommand.execute(request);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(changeSet2);
    assertThat(cmdContext.open().tip().getModifiedFiles().size()).isEqualTo(1);
    assertThat(cmdContext.open().tip().getModifiedFiles().get(0)).isEqualTo("a.txt");
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldThrowFileAlreadyExistsException() throws IOException {

    File testFile = temporaryFolder.newFile();
    try (FileOutputStream fos = new FileOutputStream(testFile)) {
      fos.write(21);
    }

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));

    hgModifyCommand.execute(request);

    try (FileOutputStream fos = new FileOutputStream(testFile)) {
      fos.write(42);
    }
    ModifyCommandRequest request2 = new ModifyCommandRequest();
    request2.setCommitMessage(" Now i really found the answer");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));
    request2.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));

    hgModifyCommand.execute(request2);
  }

  @Test
  public void shouldModifyExistingFile() throws IOException {
    File testFile = temporaryFolder.newFile("a.txt");

    try (FileOutputStream fos = new FileOutputStream(testFile)) {
      fos.write(42);
    }
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.ModifyFileRequest("a.txt", testFile));

    String changeSet2 = hgModifyCommand.execute(request);

    assertThat(cmdContext.open().tip().getNode()).isEqualTo(changeSet2);
    assertThat(cmdContext.open().tip().getModifiedFiles().size()).isEqualTo(1);
    assertThat(cmdContext.open().tip().getModifiedFiles().get(0)).isEqualTo(testFile.getName());
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionIfFileDoesNotExist() throws IOException {
    File testFile = temporaryFolder.newFile("Answer.txt");

    try (FileOutputStream fos = new FileOutputStream(testFile)) {
      fos.write(42);
    }
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.ModifyFileRequest("Answer.txt", testFile));

    hgModifyCommand.execute(request);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNPEIfAuthorIsMissing() throws IOException {
    File testFile = temporaryFolder.newFile();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));
    request.setCommitMessage("I found the answer");
    hgModifyCommand.execute(request);
  }

  @Test(expected = NullPointerException.class)
  public void shouldThrowNPEIfCommitMessageIsMissing() throws IOException {
    File testFile = temporaryFolder.newFile();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("Answer.txt", testFile, false));
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));
    hgModifyCommand.execute(request);
  }

  @Test(expected = NoChangesMadeException.class)
  public void shouldThrowNoChangesMadeExceptionIfEmptyLocalChangesetAfterRequest() {
    hgModifyCommand.execute(new ModifyCommandRequest());
  }

  @Test
  public void shouldExtractSimpleMessage() {
    Matcher matcher = AbstractWorkingCopyCommand.HG_MESSAGE_PATTERN.matcher("[SCM] This is a simple message");
    matcher.matches();
    assertThat(matcher.group(1)).isEqualTo("This is a simple message");
  }

  @Test
  public void shouldExtractErrorMessage() {
    Matcher matcher = AbstractWorkingCopyCommand.HG_MESSAGE_PATTERN.matcher("[SCM] Error: This is an error message");
    matcher.matches();
    assertThat(matcher.group(1)).isEqualTo("This is an error message");
  }

  @Test(expected = ScmConstraintViolationException.class)
  public void shouldFailIfPathInHgMetadata() throws IOException {
    File testFile = temporaryFolder.newFile("a.txt");

    try (FileOutputStream fos = new FileOutputStream(testFile)) {
      fos.write(42);
    }
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest(".hg/some.txt", testFile, true));

    hgModifyCommand.execute(request);
  }

  @Test(expected = ScmConstraintViolationException.class)
  public void shouldThrowErrorIfRelativePathIsOutsideOfWorkdir() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/../../../../g.txt", false));

    hgModifyCommand.execute(request);
  }

  @Test
  public void shouldRenameFile() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/g.txt", false));

    hgModifyCommand.execute(request);
    assertThat(cmdContext.open().tip().getDeletedFiles()).contains(new File("a.txt").toString());
    assertThat(cmdContext.open().tip().getAddedFiles()).contains(new File("g.txt").toString());
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldThrowAlreadyExistsException() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/c", false));

    hgModifyCommand.execute(request);
  }

  @Test
  public void shouldRenameFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "/notc", false));

    hgModifyCommand.execute(request);
    assertThat(cmdContext.open().tip().getDeletedFiles()).contains(new File("c/d.txt").toString());
    assertThat(cmdContext.open().tip().getDeletedFiles()).contains(new File("c/e.txt").toString());
    assertThat(cmdContext.open().tip().getAddedFiles()).contains(new File("notc/d.txt").toString());
    assertThat(cmdContext.open().tip().getAddedFiles()).contains(new File("notc/e.txt").toString());
  }

  @Test
  public void shouldMoveFileToExistingFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/c/z.txt", false));

    hgModifyCommand.execute(request);
    assertThat(cmdContext.open().tip().getDeletedFiles()).contains(new File("a.txt").toString());
    assertThat(cmdContext.open().tip().getAddedFiles()).contains(new File("c/z.txt").toString());
    assertThat(cmdContext.open().tip().getDeletedFiles()).doesNotContain(new File("c/d.txt").toString());
    assertThat(cmdContext.open().tip().getDeletedFiles()).doesNotContain(new File("c/e.txt").toString());
  }

  @Test
  public void shouldMoveFolderToExistingFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("g/h", "/y/h", false));

    hgModifyCommand.execute(request);
    assertThat(cmdContext.open().tip().getDeletedFiles()).contains(new File("g/h/j.txt").toString());
    assertThat(cmdContext.open().tip().getAddedFiles()).contains(new File("y/h/j.txt").toString());
  }

  @Test
  public void shouldMoveFileToNonExistentFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/y/z.txt", false));

    hgModifyCommand.execute(request);
    assertThat(cmdContext.open().tip().getDeletedFiles()).contains(new File("a.txt").toString());
    assertThat(cmdContext.open().tip().getAddedFiles()).contains(new File("y/z.txt").toString());
  }

  @Test
  public void shouldMoveFolderToNonExistentFolder() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "/j/k/c", false));

    hgModifyCommand.execute(request);
    assertThat(cmdContext.open().tip().getDeletedFiles()).contains(new File("c/d.txt").toString());
    assertThat(cmdContext.open().tip().getDeletedFiles()).contains(new File("c/e.txt").toString());
    assertThat(cmdContext.open().tip().getAddedFiles()).contains(new File("j/k/c/d.txt").toString());
    assertThat(cmdContext.open().tip().getAddedFiles()).contains(new File("j/k/c/e.txt").toString());
  }

  @Test
  public void shouldMoveFileWithOverwrite() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "b.txt", true));

    hgModifyCommand.execute(request);
    assertThat(cmdContext.open().tip().getDeletedFiles()).contains(new File("a.txt").toString());
    assertThat(cmdContext.open().tip().getModifiedFiles()).contains(new File("b.txt").toString());
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldFailMoveAndKeepFilesWhenSourceAndTargetAreTheSame() {
    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "c", false));

    hgModifyCommand.execute(request);
  }

  private ModifyCommandRequest prepareModifyCommandRequest() {
    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("Make some changes");
    request.setAuthor(new Person("Arthur Dent", "dent@hitchhiker.com"));
    return request;
  }
}
