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
import org.junit.Test;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;

import java.io.IOException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


public class HgBrowseCommandTest extends AbstractHgCommandTestBase {

  @Test
  public void testBrowseWithFilePath() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setPath("a.txt");
    FileObject file = new HgBrowseCommand(cmdContext).getBrowserResult(request).getFile();
    assertEquals("a.txt", file.getName());
    assertFalse(file.isDirectory());
    assertTrue(file.getChildren() == null || file.getChildren().isEmpty());
  }

  @Test
  public void testBrowse() throws IOException {
    Collection<FileObject> foList = getRootFromTip(new BrowseCommandRequest());

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt", "b.txt", "f.txt");

    FileObject a = getFileObject(foList, "a.txt");
    FileObject c = getFileObject(foList, "c");

    assertFalse(a.isDirectory());
    assertEquals("a.txt", a.getName());
    assertEquals("a.txt", a.getPath());
    assertEquals("added new line for blame", a.getDescription().get());
    assertTrue(a.getLength().getAsLong() > 0);
    checkDate(a.getCommitDate().getAsLong());
    assertTrue(c.isDirectory());
    assertEquals("c", c.getName());
    assertEquals("c", c.getPath());
  }

  @Test
  public void testBrowseShouldResolveBranchForRevision() throws IOException {
    String defaultBranchRevision = new LogCommand(cmdContext.open()).rev("default").single().getNode();

    BrowseCommandRequest browseCommandRequest = new BrowseCommandRequest();
    browseCommandRequest.setRevision("default");

    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(browseCommandRequest);

    assertThat(result.getRevision()).isEqualTo(defaultBranchRevision);
  }

  @Test
  public void testBrowseSubDirectory() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setPath("c");

    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(request);

    assertNotNull(result);

    FileObject c = result.getFile();
    assertEquals("c", c.getName());
    Collection<FileObject> foList = c.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("d.txt", "e.txt");

    assertNotNull(foList);
    assertFalse(foList.isEmpty());
    assertEquals(2, foList.size());

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

    assertNotNull(d);
    assertFalse(d.isDirectory());
    assertEquals("d.txt", d.getName());
    assertEquals("c/d.txt", d.getPath());
    assertEquals("added file d and e in folder c", d.getDescription().get());
    assertTrue(d.getLength().getAsLong() > 0);
    checkDate(d.getCommitDate().getAsLong());
    assertNotNull(e);
    assertFalse(e.isDirectory());
    assertEquals("e.txt", e.getName());
    assertEquals("c/e.txt", e.getPath());
    assertEquals("added file d and e in folder c", e.getDescription().get());
    assertTrue(e.getLength().getAsLong() > 0);
    checkDate(e.getCommitDate().getAsLong());
  }

  @Test
  public void testDisableLastCommit() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setDisableLastCommit(true);

    Collection<FileObject> foList = getRootFromTip(request);

    FileObject a = getFileObject(foList, "a.txt");

    assertFalse(a.getDescription().isPresent());
    assertFalse(a.getCommitDate().isPresent());
  }

  @Test
  public void testRecursive() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setRecursive(true);

    BrowserResult result = new HgBrowseCommand(cmdContext).getBrowserResult(request);

    assertNotNull(result);

    FileObject root = result.getFile();
    Collection<FileObject> foList = root.getChildren();

    assertNotNull(foList);
    assertFalse(foList.isEmpty());
    assertEquals(4, foList.size());

    FileObject c = getFileObject(foList, "c");
    assertTrue(c.isDirectory());
    assertEquals(2, c.getChildren().size());
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

    assertNotNull(result);

    FileObject root = result.getFile();
    Collection<FileObject> foList = root.getChildren();

    assertNotNull(foList);
    assertThat(foList).extracting("name").containsExactly("c", "a.txt", "b.txt", "f.txt");

    return foList;
  }
}
