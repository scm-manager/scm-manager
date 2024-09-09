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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.SubRepository;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


public class SvnBrowseCommandTest extends AbstractSvnCommandTestBase {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testBrowseWithFilePath() {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setPath("a.txt");
    FileObject file = createCommand().getBrowserResult(request).getFile();
    assertEquals("a.txt", file.getName());
    assertFalse(file.isDirectory());
    assertTrue(file.getChildren() == null || file.getChildren().isEmpty());
  }

  @Test
  public void testBrowse() {
    BrowserResult result = createCommand().getBrowserResult(new BrowseCommandRequest());

    assertNotNull(result);

    Collection<FileObject> foList = result.getFile().getChildren();

    assertThat(foList).extracting("name").containsExactly("c", "a.txt");

    Iterator<FileObject> iterator = foList.iterator();
    FileObject c = iterator.next();
    FileObject a = iterator.next();

    assertFalse(a.isDirectory());
    assertEquals("a.txt", a.getName());
    assertEquals("a.txt", a.getPath());
    assertEquals("added line for blame test", a.getDescription().get());
    assertTrue(a.getLength().getAsLong() > 0);
    checkDate(a.getCommitDate().getAsLong());
    assertTrue(c.isDirectory());
    assertEquals("c", c.getName());
    assertEquals("c", c.getPath());
  }

  /**
   * Method description
   *
   * @throws IOException
   */
  @Test
  public void testBrowseSubDirectory() {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setPath("c");

    BrowserResult result = createCommand().getBrowserResult(request);

    assertNotNull(result);

    Collection<FileObject> foList = result.getFile().getChildren();

    assertThat(foList).extracting("name").containsExactly("d.txt", "e.txt");

    Iterator<FileObject> iterator = foList.iterator();
    FileObject d = iterator.next();
    FileObject e = iterator.next();

    assertNotNull(d);
    assertFalse(d.isDirectory());
    assertEquals("d.txt", d.getName());
    assertEquals("c/d.txt", d.getPath());
    assertEquals("added d and e in folder c", d.getDescription().get());
    assertTrue(d.getLength().getAsLong() > 0);
    checkDate(d.getCommitDate().getAsLong());
    assertNotNull(e);
    assertFalse(e.isDirectory());
    assertEquals("e.txt", e.getName());
    assertEquals("c/e.txt", e.getPath());
    assertEquals("added d and e in folder c", e.getDescription().get());
    assertTrue(e.getLength().getAsLong() > 0);
    checkDate(e.getCommitDate().getAsLong());
  }

  @Test
  public void testDisableLastCommit() {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setDisableLastCommit(true);

    BrowserResult result = createCommand().getBrowserResult(request);

    assertNotNull(result);

    Collection<FileObject> foList1 = result.getFile().getChildren();

    assertNotNull(foList1);
    assertFalse(foList1.isEmpty());
    assertEquals(2, foList1.size());

    Collection<FileObject> foList = foList1;

    FileObject a = getFileObject(foList, "a.txt");

    assertFalse(a.getDescription().isPresent());
    assertFalse(a.getCommitDate().isPresent());
  }

  @Test
  public void testRecursive() {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setRecursive(true);
    BrowserResult result = createCommand().getBrowserResult(request);

    assertNotNull(result);

    Collection<FileObject> foList = result.getFile().getChildren();

    assertNotNull(foList);
    assertFalse(foList.isEmpty());
    assertEquals(2, foList.size());

    FileObject c = getFileObject(foList, "c");
    assertEquals("c", c.getName());
    assertTrue(c.isDirectory());
    assertEquals(2, c.getChildren().size());
  }

  @Test
  public void testLimit() {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setLimit(1);
    BrowserResult result = createCommand().getBrowserResult(request);

    assertNotNull(result);

    Collection<FileObject> foList = result.getFile().getChildren();

    assertThat(foList).extracting("name").containsExactly("c", "a.txt");
    assertThat(result.getFile().isTruncated()).isTrue();
  }

  @Test
  public void testOffset() {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setOffset(1);
    BrowserResult result = createCommand().getBrowserResult(request);

    assertNotNull(result);

    Collection<FileObject> foList = result.getFile().getChildren();

    assertThat(foList).isEmpty();
  }

  @Test
  public void testRecursiveLimit() throws IOException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setLimit(4);
    request.setRecursive(true);

    FileObject root = createCommand().getBrowserResult(request).getFile();

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

    FileObject root = createCommand().getBrowserResult(request).getFile();

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

    FileObject root = createCommand().getBrowserResult(request).getFile();

    Collection<FileObject> foList = root.getChildren();

    assertThat(foList)
      .extracting("name")
      .containsExactly("c", "a.txt");

    FileObject c = getFileObject(foList, "c");

    Collection<FileObject> cChildren = c.getChildren();
    assertThat(cChildren)
      .extracting("name")
      .containsExactly("e.txt");
  }

  @Test
  public void shouldNotAddSubRepositoryIfNotSetInProperties() {
    BrowserResult browserResult = new SvnBrowseCommand(createContext()).getBrowserResult(new BrowseCommandRequest());

    boolean containsSubRepository = browserResult.getFile().getChildren()
      .stream()
      .anyMatch(c -> c.getSubRepository() != null);

    assertFalse(containsSubRepository);
  }

  @Test
  public void shouldAddSubRepositoryIfSetInProperties() throws IOException, SVNException {
    String externalLink = "https://scm-manager.org/svn-repo";
    SvnContext svnContext = setProp("svn:externals", "external -r1 " + externalLink);

    BrowserResult browserResult = new SvnBrowseCommand(svnContext).getBrowserResult(new BrowseCommandRequest());

    boolean containsSubRepository = browserResult.getFile().getChildren()
      .stream()
      .anyMatch(c -> c.getSubRepository().getRepositoryUrl().equals(externalLink));

    assertTrue(containsSubRepository);
  }

  private SvnContext setProp(String propName, String propValue) throws SVNException, IOException {
    SvnContext context = createContext();
    SVNClientManager client = SVNClientManager.newInstance();

    File workingCopyDirectory = temporaryFolder.newFolder("working-copy");

    SVNURL url = SVNURL.fromFile(context.getDirectory());
    client.getUpdateClient().doCheckout(url, workingCopyDirectory, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.INFINITY, true);

    client.getWCClient().doSetProperty(workingCopyDirectory, propName, SVNPropertyValue.create(propValue), true, SVNDepth.UNKNOWN, null, null);
    client.getCommitClient().doCommit(new File[]{workingCopyDirectory}, false, "set prop", null, null, false, false, SVNDepth.UNKNOWN);
    return context;
  }


  private SvnBrowseCommand createCommand() {
    return new SvnBrowseCommand(createContext());
  }



  private FileObject getFileObject(Collection<FileObject> foList, String name) {
    return foList.stream()
      .filter(f -> name.equals(f.getName()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("file " + name + " not found"));
  }

}
