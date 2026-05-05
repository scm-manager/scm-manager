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

import org.javahg.commands.LogCommand;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;

import java.io.IOException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class HgBrowseCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void testBrowseWithFilePath() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setPath("a.txt");
    FileObject file = new HgBrowseCommand(cmdContext).getBrowserResult(request).getFile();

    assertThat(file.getName()).isEqualTo("a.txt");
    assertThat(file.isDirectory()).isFalse();
    assertThat(file.getChildren()).isNullOrEmpty();
  }

  @Test
  public void testBrowse() throws IOException {
    Collection<FileObject> foList = getRootFromTip(new BrowseCommandRequest());

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt", "b.txt", "f.txt");

    FileObject a = getFileObject(foList, "a.txt");
    FileObject c = getFileObject(foList, "c");

    assertThat(a.getName()).isEqualTo("a.txt");
    assertThat(a.getPath()).isEqualTo("a.txt");
    assertThat(a.isDirectory()).isFalse();
    assertThat(a.getDescription()).hasValue("added new line for blame");
    assertThat(a.getLength().getAsLong()).isGreaterThan(0);
    checkDate(a.getCommitDate().getAsLong());

    assertThat(c.getName()).isEqualTo("c");
    assertThat(c.getPath()).isEqualTo("c");
    assertThat(c.isDirectory()).isTrue();
  }

  @Test
  public void testBrowseWithRevision() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setRevision("2baab8e80280ef05a9aa76c49c76feca2872afb7");

    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(request);

    assertThat(result.isModifiable()).isFalse();
  }

  @Test
  public void testBrowseShouldResolveBranchForRevision() throws IOException {
    String defaultBranchRevision = new LogCommand(cmdContext.open()).rev("default").single().getNode();

    BrowseCommandRequest browseCommandRequest = new BrowseCommandRequest();
    browseCommandRequest.setRevision("default");

    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(browseCommandRequest);

    assertThat(result.isModifiable()).isTrue();
    assertThat(result.getRevision()).isEqualTo(defaultBranchRevision);
  }

  @Test
  public void testBrowseSubDirectory() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setPath("c");

    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(request);

    assertThat(result).isNotNull();

    FileObject c = result.getFile();
    assertThat(c.getName()).isEqualTo("c");
    Collection<FileObject> foList = c.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("d.txt", "e.txt");

    assertThat(foList).isNotNull();
    assertThat(foList).hasSize(2);

    FileObject d = null;
    FileObject e = null;

    for (FileObject f : foList)
    {
      if ("d.txt".equals(f.getName()))
      {
        d = f;
      }
      else if ("e.txt".equals(f.getName()))
      {
        e = f;
      }
    }

    assertThat(d).isNotNull();
    assertThat(d.isDirectory()).isFalse();
    assertThat(d.getName()).isEqualTo("d.txt");
    assertThat(d.getPath()).isEqualTo("c/d.txt");
    assertThat(d.getDescription()).hasValue("added file d and e in folder c");
    assertThat(d.getLength().getAsLong()).isGreaterThan(0);
    checkDate(d.getCommitDate().getAsLong());

    assertThat(e).isNotNull();
    assertThat(e.isDirectory()).isFalse();
    assertThat(e.getName()).isEqualTo("e.txt");
    assertThat(e.getPath()).isEqualTo("c/e.txt");
    assertThat(e.getDescription()).hasValue("added file d and e in folder c");
    assertThat(e.getLength().getAsLong()).isGreaterThan(0);
    checkDate(e.getCommitDate().getAsLong());
  }

  @Test
  public void testDisableLastCommit() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setDisableLastCommit(true);

    Collection<FileObject> foList = getRootFromTip(request);

    FileObject a = getFileObject(foList, "a.txt");

    assertThat(a.getDescription()).isEmpty();
    assertThat(a.getCommitDate()).isEmpty();
  }

  @Test
  public void testRecursive() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setRecursive(true);

    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(request);

    assertThat(result).isNotNull();

    FileObject root = result.getFile();
    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).isNotNull();
    assertThat(foList).hasSize(4);

    FileObject c = getFileObject(foList, "c");
    assertThat(c.isDirectory()).isTrue();
    assertThat(c.getChildren()).hasSize(2);
  }

  @Test
  public void testLimit() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setLimit(1);

    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(request);
    FileObject root = result.getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).extracting("name").containsExactly("c", "a.txt");
    assertThat(root.isTruncated()).isTrue();
  }

  @Test
  public void testMultipleCallsOfSameRequest() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setLimit(1);

    for (int i = 0; i < 5; ++i) {
      HgBrowseCommand hgBrowseCommand = new HgBrowseCommand(cmdContext);
      BrowserResult result = hgBrowseCommand.getBrowserResult(request);
      FileObject root = result.getFile();

      Collection<FileObject> foList = root.getChildren();

      assertThat(foList).extracting("name").containsExactly("c", "a.txt");
      assertThat(root.isTruncated()).isTrue();
    }
  }

  @Test
  public void testOffset() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setLimit(2);
    request.setOffset(1);

    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(request);
    FileObject root = result.getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).extracting("name").containsExactly("b.txt", "f.txt");
    assertThat(root.isTruncated()).isFalse();
  }


  @Test
  public void testRecursiveLimit() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setLimit(3);
    request.setRecursive(true);

    FileObject root = new HgBrowseCommand(cmdContext).getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt");

    FileObject c = getFileObject(foList, "c");

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("d.txt", "e.txt");
  }

  @Test
  public void testRecursiveLimitInSubDir() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setLimit(1);
    request.setRecursive(true);

    FileObject root = new HgBrowseCommand(cmdContext).getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("c");

    FileObject c = getFileObject(foList, "c");

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("d.txt");
  }

  @Test
  public void testRecursiveOffset() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setOffset(1);
    request.setRecursive(true);

    FileObject root = new HgBrowseCommand(cmdContext).getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt", "b.txt", "f.txt");

    FileObject c = getFileObject(foList, "c");

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("e.txt");
  }



  private FileObject getFileObject(Collection<FileObject> foList, String name)
  {
    return foList.stream()
      .filter(f -> name.equals(f.getName()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("file " + name + " not found"));
  }

  private Collection<FileObject> getRootFromTip(BrowseCommandRequest request) throws IOException {
    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(request);

    assertThat(result).isNotNull();

    FileObject root = result.getFile();
    Collection<FileObject> foList = root.getChildren();

    assertThat(foList).isNotNull();
    assertThat(foList).extracting("name").containsExactly("c", "a.txt", "b.txt", "f.txt");

    return foList;
  }
}
