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

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.Test;
import sonia.scm.AlreadyExistsException;
import sonia.scm.BadRequestException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryHookType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;

public class GitModifyCommandTest extends GitModifyCommandTestBase {

  @Test
  public void shouldCreateCommit() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    String newRef = command.execute(request);

    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getFullMessage()).isEqualTo("test commit");
      assertThat(lastCommit.getAuthorIdent().getName()).isEqualTo("Dirk Gently");
      assertThat(newRef).isEqualTo(lastCommit.toObjectId().name());
    }
  }

  @Test
  public void shouldCreateCommitOnSelectedBranch() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.setBranch("test-branch");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    String newRef = command.execute(request);

    ObjectId commitId = ObjectId.fromString(newRef);
    try (RevWalk revWalk = new RevWalk(createContext().open())) {
      RevCommit commit = revWalk.parseCommit(commitId);
      assertThat(commit.getParent(0).name()).isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    }
  }

  @Test
  public void shouldCreateNewFile() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("new_file")).isTrue();

    assertInTree(assertions);
  }

  @Test
  public void shouldCreateNewFileWhenPathStartsWithSlash() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("/new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("new_file")).isTrue();

    assertInTree(assertions);
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldFailIfOverwritingExistingFileWithoutOverwriteFlag() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldFailIfPathAlreadyExistsAsAFile() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt/newFile", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test
  public void shouldOverwriteExistingFileIfOverwriteFlagSet() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", newFile, true));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("a.txt")).isTrue();

    assertInTree(assertions);
  }

  @Test
  public void shouldModifyExistingFile() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.ModifyFileRequest("a.txt", newFile));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("a.txt")).isTrue();

    assertInTree(assertions);
  }

  @Test(expected = NotFoundException.class)
  public void shouldFailIfFileToModifyDoesNotExist() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.ModifyFileRequest("no.such.file", newFile));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test(expected = BadRequestException.class)
  public void shouldFailIfNoChangesMade() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "b\n".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("b.txt", newFile, true));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test(expected = ConcurrentModificationException.class)
  public void shouldFailBranchDoesNotHaveExpectedRevision() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "irrelevant\n".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("irrelevant", newFile, true));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    request.setExpectedRevision("abc");

    command.execute(request);
  }

  @Test
  public void shouldDeleteExistingFile() throws IOException, GitAPIException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt"));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("a.txt")).isFalse();

    assertInTree(assertions);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenFileToDeleteDoesNotExist() {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("no/such/file"));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenBranchDoesNotExist() {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setBranch("does-not-exist");
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt"));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldFailWithNotFoundExceptionIfBranchIsNoBranch() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "irrelevant\n".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.setBranch("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("irrelevant", newFile, true));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test
  public void shouldSignCreatedCommit() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    try (Git git = new Git(createContext().open())) {

      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getRawGpgSignature()).isNotEmpty();
      assertThat(lastCommit.getRawGpgSignature()).isEqualTo(GitTestHelper.SimpleGpgSigner.getSignature());
    }
  }

  @Test
  public void shouldNotSignCreatedCommitIfSigningDisabled() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.setSign(false);
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    try (Git git = new Git(createContext().open())) {

      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getRawGpgSignature()).isNullOrEmpty();
    }
  }

  @Test
  public void shouldTriggerPostCommitHook() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    verify(transportProtocolRule.repositoryManager, description("pre receive hook event expected"))
      .fireHookEvent(argThat(argument -> argument.getType() == RepositoryHookType.PRE_RECEIVE));
    await().pollInterval(50, MILLISECONDS).atMost(1, SECONDS).untilAsserted(() ->
      verify(transportProtocolRule.repositoryManager, description("post receive hook event expected"))
        .fireHookEvent(argThat(argument -> argument.getType() == RepositoryHookType.POST_RECEIVE))
    );
  }

  @Test(expected = ScmConstraintViolationException.class)
  public void shouldFailIfPathInGitMetadata() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "other".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest(".git/ome.txt", newFile, true));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }
}
