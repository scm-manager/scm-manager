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

import com.github.sdorra.shiro.SubjectAware;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Test;
import sonia.scm.AlreadyExistsException;
import sonia.scm.BadRequestException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NotFoundException;
import sonia.scm.ScmConstraintViolationException;
import sonia.scm.repository.GitTestHelper;
import sonia.scm.repository.Person;
import sonia.scm.repository.RepositoryHookType;
import sonia.scm.user.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.description;
import static org.mockito.Mockito.verify;

public class GitModifyCommandTest extends GitModifyCommandTestBase {

  private static final String REALM = "AdminRealm";

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-move-test.zip";
  }

  @Test
  public void shouldCreateCommit() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));

    String newRef = command.execute(request);

    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      assertThat(lastCommit.getFullMessage()).isEqualTo("Make some change");
      assertThat(lastCommit.getAuthorIdent().getName()).isEqualTo("Dirk Gently");
      assertThat(lastCommit.getCommitterIdent().getName()).isEqualTo("Dirk Gently");
      assertThat(lastCommit.getCommitterIdent().getEmailAddress()).isEqualTo("dirk@holistic.det");
      assertThat(newRef).isEqualTo(lastCommit.toObjectId().name());
    }
  }

  @Test
  public void shouldCreateCommitOnSelectedBranch() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.setBranch("test-branch");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));

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

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("new_file")).isTrue();

    assertInTree(assertions);
  }

  @Test
  public void shouldCreateNewFileWhenPathStartsWithSlash() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("/new_file", newFile, false));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("new_file")).isTrue();

    assertInTree(assertions);
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldFailIfOverwritingExistingFileWithoutOverwriteFlag() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", newFile, false));

    command.execute(request);
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldFailIfPathAlreadyExistsAsAFile() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt/newFile", newFile, false));

    command.execute(request);
  }

  @Test
  public void shouldOverwriteExistingFileIfOverwriteFlagSet() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", newFile, true));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("a.txt")).isTrue();

    assertInTree(assertions);
  }

  @Test
  public void shouldModifyExistingFile() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.ModifyFileRequest("a.txt", newFile));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("a.txt")).isTrue();

    assertInTree(assertions);
  }

  @Test(expected = NotFoundException.class)
  public void shouldFailIfFileToModifyDoesNotExist() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.ModifyFileRequest("no.such.file", newFile));

    command.execute(request);
  }

  @Test(expected = BadRequestException.class)
  public void shouldFailIfNoChangesMade() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "b\n".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("b.txt", newFile, true));

    command.execute(request);
  }

  @Test(expected = ConcurrentModificationException.class)
  public void shouldFailBranchDoesNotHaveExpectedRevision() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "irrelevant\n".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("irrelevant", newFile, true));
    request.setExpectedRevision("abc");

    command.execute(request);
  }

  @Test
  public void shouldDeleteExistingFile() throws IOException, GitAPIException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt", false));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("a.txt")).isFalse();

    assertInTree(assertions);
  }

  @Test
  public void shouldDeleteExistingDirectory() throws IOException, GitAPIException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("c", true));

    command.execute(request);

    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      try (RevWalk walk = new RevWalk(git.getRepository())) {
        RevCommit commit = walk.parseCommit(lastCommit);
        ObjectId treeId = commit.getTree().getId();
        TreeWalk treeWalk = new TreeWalk(git.getRepository());
        treeWalk.setRecursive(true);
        treeWalk.addTree(treeId);
        while (treeWalk.next()) {
          if (treeWalk.getPathString().startsWith("c/")) {
            fail("directory should be deleted");
          }
        }
      }
    }
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenFileToDeleteDoesNotExist() {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("no/such/file", false));

    command.execute(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenBranchDoesNotExist() {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.setBranch("does-not-exist");
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt", false));

    command.execute(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldFailWithNotFoundExceptionIfBranchIsNoBranch() throws IOException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "irrelevant\n".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.setBranch("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("irrelevant", newFile, true));

    command.execute(request);
  }

  @Test
  public void shouldSignCreatedCommit() throws IOException, GitAPIException {
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));

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

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.setSign(false);
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));

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

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));

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

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.CreateFileRequest(".git/ome.txt", newFile, true));

    command.execute(request);
  }

  @Test(expected = ScmConstraintViolationException.class)
  public void shouldThrowErrorIfRelativePathIsOutsideOfWorkdir() {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("g/h/c", "/../../../../b", false));

    command.execute(request);
  }

  @Test
  public void shouldRenameFile() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("b.txt", "/d.txt", false));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> {
      assertThat(canonicalTreeParser.findFile("b.txt")).isFalse();
      assertThat(canonicalTreeParser.findFile("d.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldThrowAlreadyExistsException() {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/c", false));

    command.execute(request);
  }

  @Test
  public void shouldRenameFolder() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "/notc", false));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> {
      assertThat(canonicalTreeParser.findFile("c/d.txt")).isFalse();
      assertThat(canonicalTreeParser.findFile("c/e.txt")).isFalse();
      assertThat(canonicalTreeParser.findFile("notc/d.txt")).isTrue();
      assertThat(canonicalTreeParser.findFile("notc/e.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  @Test
  public void shouldMoveFileToExistingFolder() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/c/z.txt", false));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> {
      assertThat(canonicalTreeParser.findFile("a.txt")).isFalse();
      assertThat(canonicalTreeParser.findFile("c/z.txt")).isTrue();
      assertThat(canonicalTreeParser.findFile("c/d.txt")).isTrue();
      assertThat(canonicalTreeParser.findFile("c/e.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  @Test
  public void shouldMoveFolderToExistingFolder() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("g/h", "/g/k/h", false));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> {
      assertThat(canonicalTreeParser.findFile("g/h/j.txt")).isFalse();
      assertThat(canonicalTreeParser.findFile("g/k/h/j.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  @Test
  public void shouldMoveFileToNonExistentFolder() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/y/z.txt", false));

    command.execute(request);

    TreeAssertions assertions = fileFinder -> {
      assertThat(fileFinder.findFile("a.txt")).isFalse();
      assertThat(fileFinder.findFile("y/z.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  @Test
  public void shouldMoveFileWithOverwrite() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "/b.txt", true));

    command.execute(request);

    TreeAssertions assertions = fileFinder -> {
      assertThat(fileFinder.findFile("a.txt")).isFalse();
      assertThat(fileFinder.findFile("b.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  @Test
  public void shouldMoveFolderToNonExistentFolder() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "/j/k/c", false));

    command.execute(request);

    TreeAssertions assertions = fileFinder -> {
      assertThat(fileFinder.findFile("c/d.txt")).isFalse();
      assertThat(fileFinder.findFile("c/e.txt")).isFalse();
      assertThat(fileFinder.findFile("j/k/c/d.txt")).isTrue();
      assertThat(fileFinder.findFile("j/k/c/e.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldFailMoveAndKeepFilesWhenSourceAndTargetAreTheSame() {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = prepareModifyCommandRequest();
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "c", false));

    command.execute(request);
  }

  private ModifyCommandRequest prepareModifyCommandRequest() {
    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("Make some change");
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    return request;
  }

  private ModifyCommandRequest prepareModifyCommandRequestWithoutAuthorEmail() {
    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setAuthor(new Person("Dirk Gently", ""));
    request.setCommitMessage("Make some change");
    return request;
  }

}
