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

import org.junit.Test;
import sonia.scm.repository.FileObject;
import sonia.scm.web.lfs.LfsBlobStoreFactory;

import java.io.IOException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static sonia.scm.repository.spi.SyncAsyncExecutors.synchronousExecutor;

/**
 * Unit tests for {@link GitBrowseCommand}.
 *
 */
public class GitBrowseCommand_RecursiveDirectoryNameTest extends AbstractGitCommandTestBase {

  private final LfsBlobStoreFactory lfsBlobStoreFactory = mock(LfsBlobStoreFactory.class);

  @Test
  public void testRecursive() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setRecursive(true);

    FileObject root = createCommand().getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt", "b.txt", "cw", "f.txt");

    FileObject c = findFolder(foList);

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("d.txt", "e.txt");
  }

  private FileObject findFolder(Collection<FileObject> foList) {
    return foList.stream()
      .filter(f -> "c".equals(f.getName()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("file " + "c" + " not found"));
  }

  @Override
  protected String getZippedRepositoryResource()
  {
    return "sonia/scm/repository/spi/scm-git-spi-browse-recursive-test.zip";
  }

  private GitBrowseCommand createCommand() {
    return new GitBrowseCommand(createContext(), lfsBlobStoreFactory, synchronousExecutor());
  }
}
