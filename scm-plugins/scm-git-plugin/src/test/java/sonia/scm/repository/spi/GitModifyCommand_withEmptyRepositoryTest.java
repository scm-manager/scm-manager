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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;
import sonia.scm.repository.Person;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("java:S5976") // using parameterized tests in this class is not useful because we would miss the descriptions
public class GitModifyCommand_withEmptyRepositoryTest extends GitModifyCommandTestBase {

  @Test
  public void shouldCreateNewFileInEmptyRepository() throws IOException, GitAPIException {
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
  public void shouldCreateCommitOnMasterByDefault() throws IOException, GitAPIException {
    createContext().getGlobalConfig().setDefaultBranch("");

    executeModifyCommand();

    try (Git git = new Git(createContext().open())) {
      List<Ref> branches = git.branchList().call();
      assertThat(branches).extracting("name").containsExactly("refs/heads/master");
    }
  }

  @Test
  public void shouldCreateCommitOnMasterIfSetExplicitly() throws IOException, GitAPIException {
    createContext().getGlobalConfig().setDefaultBranch("master");

    executeModifyCommand();

    try (Git git = new Git(createContext().open())) {
      List<Ref> branches = git.branchList().call();
      assertThat(branches).extracting("name").containsExactly("refs/heads/master");
    }
  }

  @Test
  public void shouldCreateCommitWithConfiguredDefaultBranch() throws IOException, GitAPIException {
    createContext().getGlobalConfig().setDefaultBranch("main");

    executeModifyCommand();

    try (Git git = new Git(createContext().open())) {
      List<Ref> branches = git.branchList().call();
      assertThat(branches).extracting("name").containsExactly("refs/heads/main");
    }
  }

  @Test
  public void shouldCreateCommitWithBranchFromRequestIfPresent() throws IOException, GitAPIException {
    createContext().getGlobalConfig().setDefaultBranch("main");

    ModifyCommandRequest request = createRequest();
    request.setBranch("different");
    createCommand().execute(request);

    try (Git git = new Git(createContext().open())) {
      List<Ref> branches = git.branchList().call();
      assertThat(branches).extracting("name").containsExactly("refs/heads/different");
    }
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-empty-repo.zip";
  }

  @Override
  RevCommit getLastCommit(Git git) throws GitAPIException, IOException {
    return git.log().setMaxCount(1).all().call().iterator().next();
  }

  private void executeModifyCommand() throws IOException {
    createCommand().execute(createRequest());
  }

  private ModifyCommandRequest createRequest() throws IOException {
    File newFile = Files.write(temporaryFolder.newFile().toPath(), "new content".getBytes()).toFile();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("initial commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    return request;
  }
}
