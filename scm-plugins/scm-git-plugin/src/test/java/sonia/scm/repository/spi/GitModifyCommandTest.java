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

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-spi-move-test.zip";
  }

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
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt", false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> assertThat(canonicalTreeParser.findFile("a.txt")).isFalse();

    assertInTree(assertions);
  }

  @Test
  public void shouldDeleteExistingDirectory() throws IOException, GitAPIException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("c", true));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

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

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("no/such/file", false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test(expected = NotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenBranchDoesNotExist() {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setBranch("does-not-exist");
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.DeleteFileRequest("a.txt", false));
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

  /*
    # Ordner verschieben/umbenennen

    Ordner: /g/k
    Move: /g/y
    Oder Move: y
    Ordner /g/y existiert nicht
    Erwartet: Alle Dateien/Ordner aus /g/k liegen unter /g/y

    Ordner: /g/k
    Move: /x/y
    Oder Move: ../../x/y (???)
    Ordner /x existiert nicht
    Erwartet: Alle Dateien/Ordner aus /g/k liegen unter /x/y

    # Datei verschieben/umbenennen

    Datei: /g/h/c
    Move: /
    Oder Move: ../..
    Erwartet: Die Datei c liegt unter /

    Datei: /g/h/j.txt
    Move: i
    Oder Move: /g/h/i
    Datei i existiert bereits
    Erwartet: Fehler!

    Datei: /g/h/j.txt
    Move: x.txt
    Oder Move: ./x.txt
    Oder Move: /g/h/x.txt
    Datei x.txt existiert nicht
    Erwartet: Die Datei wurde umbenannt in /g/h/x.txt

    Datei: /g/h/c
    Move: /
    Oder Move: ../..
    Ordner c existiert bereits
    Erwartet: Fehler!

    Datei: /g/h/c
    Move: /y.txt
    Oder Move: ../../y.txt
    Datei /y.txt existiert nicht
    Erwartet: Die Datei c wurde nach / verschoben und in y.txt umbenannt

    Datei: /g/h/c
    Move: /g/k
    Oder Move: ../k
    Ordner /g/k existiert bereits
    Erwartet: Die Datei c wurde nach /g/k verschoben


    # Sonderfall



   */


  /**
   *     Ordner: /g/h
   *     Move: ..
   *     Erwartet: Alle Dateien/Ordner aus /g/h liegen unter /h
   */
  @Test
  public void moveFileCaseA1() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please rename this file");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("/g/h", ".."));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> {
      assertThat(canonicalTreeParser.findFile("h/j.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  /**
   *     Ordner: /g/h
   *     Move: /
   *
   *     /h
   *     Erwartet: Alle Dateien/Ordner aus /g/h liegen unter /h
   */
  @Test
  public void moveFileCaseA2() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please rename this file");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("/g/h", "/")); // "/ + h"

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> {
      assertThat(canonicalTreeParser.findFile("h/j.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  /**
   *     Ordner: /g/k
   *     Move: /g/h
   *     Ordner /g/h existiert bereits
   *     Erwartet: Alle Dateien/Ordner aus /g/k liegen unter /g/h/k
   */
  @Test
  public void moveFileCaseB1() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please rename this file");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("/g/k", "/g/h"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> {
      assertThat(canonicalTreeParser.findFile("g/h/k/l.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  /**
   *     Ordner: /g/k
   *     Move: h
   *     Ordner /g/h existiert bereits
   *     Erwartet: Alle Dateien/Ordner aus /g/k liegen unter /g/h/k
   */
  @Test
  public void moveFileCaseB2() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please rename this file");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("/g/k", "h"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> {
      assertThat(canonicalTreeParser.findFile("g/h/k/l.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  /**
   *     Ordner: /g/h/c
   *     Move: ../../../..
   *     Erwartet: Fehler vom Backend, da Datei/Ordner aus dem Repo verschoben wurde
   */
  @Test(expected = ScmConstraintViolationException.class)
  public void shouldThrowErrorIfRelativePathIsOutsideOfWorkdir() {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please rename this file");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("/g/h/c", "../../../.."));

    command.execute(request);
  }

  // Simple cases

  @Test
  public void shouldRenameFile() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please rename this file");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("b.txt", "d.txt"));

    command.execute(request);

    TreeAssertions assertions = canonicalTreeParser -> {
      assertThat(canonicalTreeParser.findFile("b.txt")).isFalse();
      assertThat(canonicalTreeParser.findFile("d.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  @Test
  public void shouldRenameFolder() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please move this folder");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "notc"));

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

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please move this file");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "c/z.txt"));

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
  public void shouldMoveFileToNonExistentFolder() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please move this file");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("a.txt", "y/z.txt"));

    command.execute(request);

    TreeAssertions assertions = fileFinder -> {
      assertThat(fileFinder.findFile("a.txt")).isFalse();
      assertThat(fileFinder.findFile("y/z.txt")).isTrue();
    };

    assertInTree(assertions);
  }

  @Test
  public void shouldMoveFolderToNonExistentFolder() throws GitAPIException, IOException {
    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("please move this file");
    request.setAuthor(new Person("Peter Pan", "peter@pan.net"));
    request.addRequest(new ModifyCommandRequest.MoveRequest("c", "j/k/c"));

    command.execute(request);

    TreeAssertions assertions = fileFinder -> {
      assertThat(fileFinder.findFile("c/d.txt")).isFalse();
      assertThat(fileFinder.findFile("c/e.txt")).isFalse();
      assertThat(fileFinder.findFile("j/k/c/d.txt")).isTrue();
      assertThat(fileFinder.findFile("j/k/c/e.txt")).isTrue();
    };

    assertInTree(assertions);
  }
}
