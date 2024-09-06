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

package sonia.scm.repository.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.SubRepository;
import sonia.scm.repository.spi.BrowseCommand;
import sonia.scm.repository.spi.BrowseCommandRequest;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BrowserResultCollapserTest {

  @Mock
  private BrowseCommand browseCommand;

  private Map<String, FileObject> browseResults;

  @BeforeEach
  void setUp() throws Exception {
    browseResults = new HashMap<>();
    lenient().when(browseCommand.getBrowserResult(any(BrowseCommandRequest.class)))
      .thenAnswer(
        (Answer<BrowserResult>) invocation -> {
          BrowseCommandRequest request = (BrowseCommandRequest) invocation.getArguments()[0];
          return createBrowserResult(browseResults.get(request.getPath()));
        }
      );
  }

  /*
    /
    ├─ folder_a
    │  └─ file
    └─ folder_b
       └─ file
   */
  @Test
  void collapseFoldersShouldNotCollapseNonEmptyFolder() throws Exception {
    FileObject root = createFolder(null, "");

    FileObject folder_a = createFolder(root, "folder_a");
    createFile(folder_a);

    FileObject folder_b = createFolder(root, "folder_b");
    createFile(folder_b);

    BrowserResult result = new BrowserResult("revision", root);
    BrowseCommandRequest request = new BrowseCommandRequest();

    new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());

    FileObject f = result.getFile();
    Collection<FileObject> children = f.getChildren();
    assertThat(children).hasSize(2);
    assertContains(children, "folder_a", "folder_a");
    assertContains(children, "folder_b", "folder_b");
  }

  /*
    /
    ├─ folder_a
    │  └─ file
    └─ folder_b
       └─ subfolder
          └─ file
   */
  @Test
  void collapseFoldersShouldCollapseFolderWithJustOneSubFolder() throws Exception {
    FileObject root = createFolder(null, "");

    FileObject folder_a = createFolder(root, "folder_a");
    createFile(folder_a);

    FileObject folder_b = createFolder(root, "folder_b");
    FileObject subfolder = createFolder(folder_b, "subfolder");
    createFile(subfolder);

    BrowserResult result = new BrowserResult("revision", root);
    BrowseCommandRequest request = new BrowseCommandRequest();

    new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());

    FileObject f = result.getFile();
    Collection<FileObject> children = f.getChildren();
    assertThat(children).hasSize(2);
    assertContains(children, "folder_a", "folder_a");
    assertContains(children, "folder_b/subfolder", "folder_b/subfolder");
  }

  /*
    /
    ├─ folder_a
    │  └─ file
    ├─ folder_b
    │  ├─ subfolder_a
    │  │  └─ subfolder_a_subfolder
    │  │     └─ file
    │  └─ subfolder_b
    └─ folder_c
       └─ subfolder_a
          └─ file
   */
  @Test
  void collapseFoldersShouldNotCollapseFolderWithMoreThanSingleSubFolder() throws Exception {
    FileObject root = createFolder(null, "");

    FileObject folder_a = createFolder(root, "folder_a");
    createFile(folder_a);

    FileObject folder_b = createFolder(root, "folder_b");
    FileObject subfolder_a = createFolder(folder_b, "subfolder_a");
    FileObject subfolder_a_subfolder = createFolder(subfolder_a, "subfolder_a_subfolder");
    createFile(subfolder_a_subfolder);
    createFolder(folder_b, "subfolder_b");

    FileObject folder_c = createFolder(root, "folder_c");
    FileObject subfolder_b = createFolder(folder_c, "subfolder_b");
    createFile(subfolder_b);

    FileObject folder_d = createFolder(root, "folder_d");
    createFolder(folder_d, "subfolder_c");

    BrowserResult result = new BrowserResult("revision", root);
    BrowseCommandRequest request = new BrowseCommandRequest();

    new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());

    FileObject f = result.getFile();
    Collection<FileObject> children = f.getChildren();
    assertThat(children).hasSize(4);
    assertContains(children, "folder_a", "folder_a");
    assertContains(children, "folder_b", "folder_b");
    assertContains(children, "folder_c/subfolder_b", "folder_c/subfolder_b");
    assertContains(children, "folder_d/subfolder_c", "folder_d/subfolder_c");
  }

  /*
    /
    ├─ folder_a
    │  └─ file
    └─ folder_b
       └─ subrepository
   */
  @Test
  void collapseFoldersShouldNotCollapseSubRepositoryFolder() throws Exception {
    FileObject root = createFolder(null, "");

    FileObject folder_a = createFolder(root, "folder_a");
    createFile(folder_a);

    FileObject folder_b = createFolder(root, "folder_b");
    FileObject subfolder = createFolder(folder_b, "subfolder");
    subfolder.setSubRepository(mock(SubRepository.class));

    BrowserResult result = new BrowserResult("revision", root);
    BrowseCommandRequest request = new BrowseCommandRequest();

    new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());

    FileObject f = result.getFile();
    Collection<FileObject> children = f.getChildren();
    assertThat(children).hasSize(2);
    assertContains(children, "folder_a", "folder_a");
    assertContains(children, "folder_b", "folder_b");
  }

  /*
    For SVN, externals must not be "browsed" because this throws a not found exception.
    /
    └─ folder_b/external
   */
  @Test
  void collapseFoldersShouldNotCollapseSvnExternal() throws Exception {
    FileObject root = createFolder(null, "");

    FileObject external = createFolder(root, "folder_b/external");
    external.setSubRepository(mock(SubRepository.class));
    lenient().when(browseCommand.getBrowserResult(argThat(request -> request.getPath().equals("folder_b/external"))))
      .thenThrow(NotFoundException.class);

    BrowserResult result = new BrowserResult("revision", root);
    BrowseCommandRequest request = new BrowseCommandRequest();

    new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());

    FileObject f = result.getFile();
    Collection<FileObject> children = f.getChildren();
    assertThat(children).hasSize(1);
    assertContains(children, "folder_b/external", "folder_b/external");
  }

  /*
    /
    ├─ scm-plugins
    │  ├─ build.gradle
    │  └─ gradle.lockfile
    ├─ scm-server
    │  ├─ src
    │  │  └─ main
    │  │     └─ java
    │  │        └─ sonia
    │  │           └─ scm
    │  │              └─ server
    │  │                 ├─ ScmServer.java
    │  │                 ├─ ScmServerDaemon.java
    │  │                 └─ ScmServerException.java
    │  ├─ build.gradle
    │  └─ gradle.lockfile
    ├─ scm-test
    │  ├─ build.gradle
    │  └─ gradle.lockfile
    ├─ .dockerignore
    └─ .editorconfig
   */
  @Test
  void collapseFoldersShouldWorkProperlyWithRealLifeExample() throws Exception {
    FileObject root = createFolder(null, "");

    FileObject scmPlugins = createFolder(root, "scm-plugins");
    createFile(scmPlugins, "build.gradle");
    createFile(scmPlugins, "gradle.lockfile");

    FileObject scmServer = createFolder(root, "scm-server");
    FileObject src = createFolder(scmServer, "src");
    FileObject main = createFolder(src, "main");
    FileObject java = createFolder(main, "java");
    FileObject sonia = createFolder(java, "sonia");
    FileObject scm = createFolder(sonia, "scm");
    FileObject server = createFolder(scm, "server");
    createFile(server, "ScmServer.java");
    createFile(server, "ScmServerDaemon.java");
    createFile(server, "ScmServerException.java");
    createFile(scmServer, "build.gradle");
    createFile(scmServer, "gradle.lockfile");

    FileObject scmTest = createFolder(root, "scm-test");
    createFile(scmTest, "build.gradle");
    createFile(scmTest, "gradle.lockfile");

    createFile(root, ".dockerignore");
    createFile(root, ".editorconfig");

    BrowserResult result = new BrowserResult("revision", scmServer);
    BrowseCommandRequest request = new BrowseCommandRequest();

    new BrowserResultCollapser().collapseFolders(browseCommand, request, result.getFile());

    FileObject f = result.getFile();
    Collection<FileObject> children = f.getChildren();
    assertThat(children).hasSize(3);
    assertContains(children, "src/main/java/sonia/scm/server", "scm-server/src/main/java/sonia/scm/server");
    assertContains(children, "build.gradle", "scm-server/build.gradle");
    assertContains(children, "gradle.lockfile", "scm-server/gradle.lockfile");
  }

  private void assertContains(Collection<FileObject> children, String name, String path) {
    assertThat(children)
      .as("%s not found", name)
      .anyMatch(c -> c.getName().equals(name) && c.getPath().equals(path));
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
    createFile(parent, "file");
  }

  private void createFile(FileObject parent, String name) {
    FileObject f = createFileObject(parent, name);
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
