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
