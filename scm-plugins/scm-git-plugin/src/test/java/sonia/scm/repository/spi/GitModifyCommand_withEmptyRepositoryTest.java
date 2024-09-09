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
  public void shouldCreateCommitOnMainByDefault() throws IOException, GitAPIException {
    createContext().getGlobalConfig().setDefaultBranch("");

    executeModifyCommand();

    try (Git git = new Git(createContext().open())) {
      List<Ref> branches = git.branchList().call();
      assertThat(branches).extracting("name").containsExactly("refs/heads/main");
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
    File newFile = Files.write(tempFolder.newFile().toPath(), "new content".getBytes()).toFile();

    ModifyCommandRequest request = new ModifyCommandRequest();
    request.setCommitMessage("initial commit");
    request.addRequest(new ModifyCommandRequest.CreateFileRequest("new_file", newFile, false));
    request.setAuthor(new Person("Dirk Gently", "dirk@holistic.det"));
    return request;
  }
}
