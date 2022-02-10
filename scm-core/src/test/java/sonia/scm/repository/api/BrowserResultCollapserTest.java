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

package sonia.scm.repository.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.spi.BrowseCommand;
import sonia.scm.repository.spi.BrowseCommandRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrowserResultCollapserTest {

  @Mock
  private BrowseCommand browseCommand;

  private Map<String, FileObject> browseResults;

  @BeforeEach
  void setUp() throws Exception {
    browseResults = new HashMap<>();
    when(browseCommand.getBrowserResult(any(BrowseCommandRequest.class)))
      .thenAnswer(
        (Answer<BrowserResult>) invocation -> {
          BrowseCommandRequest request = (BrowseCommandRequest) invocation.getArguments()[0];
          return createBrowserResult(browseResults.get(request.getPath()));
        }
      );
  }

  @Test
  void collapseFoldersShouldNotCollapseNonEmptyFolders() throws Exception {
    FileObject root = createFolder(null, "");

    FileObject not_empty_1A = createFolder(root, "not_empty_1A");
    createFile(not_empty_1A);

    FileObject not_empty_1B = createFolder(root, "not_empty_1B");
    createFile(not_empty_1B);

    BrowserResult result = new BrowserResult("revision", root);
    BrowseCommandRequest request = new BrowseCommandRequest();

    new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());

    FileObject f = result.getFile();
    Collection<FileObject> children = f.getChildren();
    assertThat(children.size()).isEqualTo(2);
    assertThat(contains(children, "not_empty_1A", "not_empty_1A")).isTrue();
    assertThat(contains(children, "not_empty_1B", "not_empty_1B")).isTrue();
  }

  @Test
  void collapseFoldersShouldCollapseSimpleEmptyFolders() throws Exception {
    FileObject root = createFolder(null, "");

    FileObject not_empty_1A = createFolder(root, "not_empty_1A");
    createFile(not_empty_1A);

    FileObject empty_1A = createFolder(root, "empty_1A");
    FileObject not_empty_2A = createFolder(empty_1A, "not_empty_2A");
    createFile(not_empty_2A);

    BrowserResult result = new BrowserResult("revision", root);
    BrowseCommandRequest request = new BrowseCommandRequest();

    new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());

    FileObject f = result.getFile();
    Collection<FileObject> children = f.getChildren();
    assertThat(children.size()).isEqualTo(2);
    assertThat(contains(children, "not_empty_1A", "not_empty_1A")).isTrue();
    assertThat(contains(children, "empty_1A/not_empty_2A", "empty_1A/not_empty_2A")).isTrue();
  }

  @Test
  void collapseFoldersShouldCollapseComplexEmptyFolders() throws Exception {
    FileObject root = createFolder(null, "");

    FileObject not_empty_1A = createFolder(root, "not_empty_1A");
    createFile(not_empty_1A);

    FileObject empty_1A = createFolder(root, "empty_1A");
    FileObject empty_2A = createFolder(empty_1A, "empty_2A");
    FileObject not_empty_3A = createFolder(empty_2A, "not_empty_3A");
    createFile(not_empty_3A);
    FileObject empty_2B = createFolder(empty_1A, "empty_2B");
    FileObject empty_3A = createFolder(empty_2B, "empty_3A");
    FileObject not_empty_4A = createFolder(empty_3A, "not_empty_4A");
    createFile(not_empty_4A);

    FileObject empty_1B = createFolder(root, "empty_1B");
    FileObject not_empty_2A = createFolder(empty_1B, "not_empty_2A");
    createFile(not_empty_2A);

    BrowserResult result = new BrowserResult("revision", root);
    BrowseCommandRequest request = new BrowseCommandRequest();

    new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());

    FileObject f = result.getFile();
    Collection<FileObject> children = f.getChildren();
    assertThat(children.size()).isEqualTo(4);
    assertThat(contains(children, "not_empty_1A", "not_empty_1A")).isTrue();
    assertThat(contains(children, "empty_1A/empty_2A/not_empty_3A", "empty_1A/empty_2A/not_empty_3A")).isTrue();
    assertThat(contains(children, "empty_1A/empty_2B/empty_3A/not_empty_4A", "empty_1A/empty_2B/empty_3A/not_empty_4A")).isTrue();
    assertThat(contains(children, "empty_1B/not_empty_2A", "empty_1B/not_empty_2A")).isTrue();
  }

  private boolean contains(Collection<FileObject> children, String name, String path) {
    return children.stream().anyMatch(c -> c.getName().equals(name) && c.getPath().equals(path));
  }

  private BrowserResult createBrowserResult(FileObject f) {
    return new BrowserResult("revision", f);
  }

  private FileObject createFolder(FileObject parent, String name) {
    FileObject f = createFileObject(parent, name);
    f.setDirectory(true);
    if (parent != null) {
      parent.addChild(f);
    }
    return f;
  }

  private void createFile(FileObject parent) {
    FileObject f = createFileObject(parent, "test.txt");
    f.setDirectory(false);
    if (parent != null) {
      parent.addChild(f);
    }
  }

  private FileObject createFileObject(FileObject parent, String name) {
    FileObject f = new FileObject();
    f.setName(name);
    String path = (parent != null && !parent.getPath().equals("") ? parent.getPath() + "/" : "") + name;
    f.setPath(path);
    browseResults.put(path, f);
    return f;
  }

}
