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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.CorruptObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import sonia.scm.AlreadyExistsException;
import sonia.scm.BadRequestException;
import sonia.scm.ConcurrentModificationException;
import sonia.scm.NotFoundException;
import sonia.scm.repository.Person;
import sonia.scm.repository.work.NoneCachingWorkingCopyPool;
import sonia.scm.repository.work.WorkdirProvider;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SubjectAware(configuration = "classpath:sonia/scm/configuration/shiro.ini", username = "admin", password = "secret")
public class GitModifyCommandTest extends AbstractGitCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Rule
  public BindTransportProtocolRule transportProtocolRule = new BindTransportProtocolRule();
  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);

  @Test
  public void shouldCreateCommit() throws IOException, GitAPIException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

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
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

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
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

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
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

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
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test(expected = AlreadyExistsException.class)
  public void shouldFailIfPathAlreadyExistsAsAFile() throws IOException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("a.txt/newFile", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test
  public void shouldOverwriteExistingFileIfOverwriteFlagSet() throws IOException, GitAPIException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

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
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

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
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.ModifyFileRequest("no.such.file", newFile));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test(expected = BadRequestException.class)
  public void shouldFailIfNoChangesMade() throws IOException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "b\n".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("b.txt", newFile, true));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  @Test(expected = ConcurrentModificationException.class)
  public void shouldFailBranchDoesNotHaveExpectedRevision() throws IOException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "irrelevant\n".getBytes()).toFile();

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
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "irrelevant\n".getBytes()).toFile();

    GitModifyCommand command = createCommand();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("test commit");
    request.setBranch("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("irrelevant", newFile, true));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));

    command.execute(request);
  }

  private void assertInTree(TreeAssertions assertions) throws IOException, GitAPIException {
    try (Git git = new Git(createContext().open())) {
      RevCommit lastCommit = getLastCommit(git);
      try (RevWalk walk = new RevWalk(git.getRepository())) {
        RevCommit commit = walk.parseCommit(lastCommit);
        ObjectId treeId = commit.getTree().getId();
        try (ObjectReader reader = git.getRepository().newObjectReader()) {
          assertions.checkAssertions(new CanonicalTreeParser(null, reader, treeId));
        }
      }
    }
  }

  private RevCommit getLastCommit(Git git) throws GitAPIException {
    return git.log().setMaxCount(1).call().iterator().next();
  }

  private GitModifyCommand createCommand() {
    return new GitModifyCommand(createContext(), new SimpleGitWorkingCopyFactory(new NoneCachingWorkingCopyPool(new WorkdirProvider())), lfsBlobStoreFactory);
  }

  @FunctionalInterface
  private interface TreeAssertions {
    void checkAssertions(CanonicalTreeParser treeParser) throws CorruptObjectException;
  }
}
