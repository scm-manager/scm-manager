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

import jakarta.annotation.Nonnull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static sonia.scm.repository.spi.SyncAsyncExecutors.synchronousExecutor;

@RunWith(MockitoJUnitRunner.class)
public class GitBrowseCommand_BrokenSubmoduleTest extends AbstractGitCommandTestBase {

  @Mock
  private LfsBlobStoreFactory lfsBlobStoreFactory;

  private GitBrowseCommand command;

  @Before
  public void createCommand() {
    command = new GitBrowseCommand(createContext(), lfsBlobStoreFactory, synchronousExecutor());
  }

  @Test
  public void testBrowse() throws IOException {
    BrowserResult result = command.getBrowserResult(new BrowseCommandRequest());
    Collection<FileObject> children = result.getFile().getChildren();

    List<String> subrepos = subRepositoriesOnly(children);
    assertThat(subrepos).containsExactly(
      "anonymous-access",
      "hasselhoffme",
      "recipes",
      "scm-redmine-plugin"
    );

    List<String> directories = directoriesOnly(children);
    assertThat(directories).containsExactly(
      "dir",
      "plugins"
      );

    List<String> files = filesOnly(children);
    assertThat(files)
      .containsExactly(
        ".gitmodules",
        "README.md",
        "test.txt"
      );
  }

  @Test
  public void testBrowseRecursive() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setRecursive(true);
    BrowserResult result = command.getBrowserResult(request);
    Collection<FileObject> children = result.getFile().getChildren();
    FileObject fileObject = children.stream().filter(f -> "plugins".equals(f.getPath())).findFirst().get();
    assertThat(fileObject.getChildren()).hasSize(3);
    List<String> subrepos = subRepositoriesOnly(fileObject.getChildren());
    assertThat(subrepos)
      .containsExactly(
        "plugins/scm-branchwp-plugin",
        "plugins/scm-jira-plugin",
        "plugins/statistic-plugin"
      );
  }

  @Nonnull
  private List<String> filesOnly(Collection<FileObject> children) {
    return children.stream().filter(f -> !f.isDirectory()).map(FileObject::getPath).collect(Collectors.toList());
  }

  @Nonnull
  private List<String> directoriesOnly(Collection<FileObject> children) {
    return children.stream()
      .filter(FileObject::isDirectory)
      .filter(f -> f.getSubRepository() == null)
      .map(FileObject::getPath)
      .collect(Collectors.toList());
  }

  @Nonnull
  private List<String> subRepositoriesOnly(Collection<FileObject> children) {
    return children.stream()
      .filter(f -> f.getSubRepository() != null)
      .map(FileObject::getPath)
      .collect(Collectors.toList());
  }

  @Override
  protected String getZippedRepositoryResource() {
    return "sonia/scm/repository/spi/scm-git-broken-submodule-repo.zip";
  }
}
