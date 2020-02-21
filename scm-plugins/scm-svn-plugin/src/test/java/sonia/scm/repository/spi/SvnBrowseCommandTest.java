/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
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

    Collection<FileObject> foList1 = result.getFile().getChildren();

    assertNotNull(foList1);
    assertFalse(foList1.isEmpty());
    assertEquals(2, foList1.size());

    Collection<FileObject> foList = foList1;

    Iterator<FileObject> iterator = foList.iterator();
    FileObject a = iterator.next();
    FileObject c = iterator.next();

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

    assertNotNull(foList);
    assertFalse(foList.isEmpty());
    assertEquals(2, foList.size());

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

    assertThat(foList).extracting("name").containsExactly("a.txt");
    assertThat(result.getFile().isTruncated()).isTrue();
  }

  @Test
  public void testOffset() {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setOffset(1);
    BrowserResult result = createCommand().getBrowserResult(request);

    assertNotNull(result);

    Collection<FileObject> foList = result.getFile().getChildren();

    assertThat(foList).extracting("name").containsExactly("c");
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
