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
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnBrowseCommandTest extends AbstractSvnCommandTestBase
{

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

  /**
   * Method description
   *
   *
   * @return
   */
  private SvnBrowseCommand createCommand()
  {
    return new SvnBrowseCommand(createContext(), repository);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param foList
   * @param name
   *
   * @return
   */
  private FileObject getFileObject(Collection<FileObject> foList, String name)
  {
    return foList.stream()
      .filter(f -> name.equals(f.getName()))
      .findFirst()
      .orElseThrow(() -> new AssertionError("file " + name + " not found"));
  }

}
