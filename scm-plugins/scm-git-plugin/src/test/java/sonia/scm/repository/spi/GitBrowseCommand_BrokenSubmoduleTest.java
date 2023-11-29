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
