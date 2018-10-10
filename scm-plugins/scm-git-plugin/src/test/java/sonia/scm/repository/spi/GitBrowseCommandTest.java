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

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;
import sonia.scm.NotFoundException;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.GitConstants;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

//~--- JDK imports ------------------------------------------------------------

/**
 * Unit tests for {@link GitBrowseCommand}.
 * 
 * @author Sebastian Sdorra
 */
public class GitBrowseCommandTest extends AbstractGitCommandTestBase
{

  @Test
  public void testGetFile() throws IOException, NotFoundException {
    BrowseCommandRequest request = new BrowseCommandRequest();
    request.setPath("a.txt");
    BrowserResult result = createCommand().getBrowserResult(request);
    FileObject fileObject = result.getFile();
    assertEquals("a.txt", fileObject.getName());
  }
  
  /**
   * Test browse command with default branch.
   */
  @Test
  public void testDefaultBranch() throws IOException, NotFoundException {
    // without default branch, the repository head should be used
    FileObject root = createCommand().getBrowserResult(new BrowseCommandRequest()).getFile();
    assertNotNull(root);

    List<FileObject> foList = root.getChildren();
    assertNotNull(foList);
    assertFalse(foList.isEmpty());

    assertEquals(4, foList.size());
    
    assertEquals("a.txt", foList.get(0).getName());
    assertEquals("b.txt", foList.get(1).getName());
    assertEquals("c", foList.get(2).getName());
    assertEquals("f.txt", foList.get(3).getName());
    
    // set default branch and fetch again
    repository.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "test-branch");

    root = createCommand().getBrowserResult(new BrowseCommandRequest()).getFile();
    assertNotNull(root);

    foList = root.getChildren();
    assertNotNull(foList);
    assertFalse(foList.isEmpty());
    assertEquals(2, foList.size());
    
    assertEquals("a.txt", foList.get(0).getName());
    assertEquals("c", foList.get(1).getName());
  }

  @Test
  public void testBrowse() throws IOException, NotFoundException {
    FileObject root = createCommand().getBrowserResult(new BrowseCommandRequest()).getFile();
    assertNotNull(root);

    List<FileObject> foList = root.getChildren();

    assertNotNull(foList);
    assertFalse(foList.isEmpty());
    assertEquals(4, foList.size());

    FileObject a = null;
    FileObject c = null;

    for (FileObject f : foList)
    {
      if ("a.txt".equals(f.getName()))
      {
        a = f;
      }
      else if ("c".equals(f.getName()))
      {
        c = f;
      }
    }

    assertNotNull(a);
    assertFalse(a.isDirectory());
    assertEquals("a.txt", a.getName());
    assertEquals("a.txt", a.getPath());
    assertEquals("added new line for blame", a.getDescription());
    assertTrue(a.getLength() > 0);
    checkDate(a.getLastModified());
    assertNotNull(c);
    assertTrue(c.isDirectory());
    assertEquals("c", c.getName());
    assertEquals("c", c.getPath());
  }

  @Test
  public void testBrowseSubDirectory() throws IOException, NotFoundException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setPath("c");

    FileObject root = createCommand().getBrowserResult(request).getFile();
    assertNotNull(root);


    List<FileObject> foList = root.getChildren();

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
    assertEquals("added file d and e in folder c", d.getDescription());
    assertTrue(d.getLength() > 0);
    checkDate(d.getLastModified());
    assertNotNull(e);
    assertFalse(e.isDirectory());
    assertEquals("e.txt", e.getName());
    assertEquals("c/e.txt", e.getPath());
    assertEquals("added file d and e in folder c", e.getDescription());
    assertTrue(e.getLength() > 0);
    checkDate(e.getLastModified());
  }

  @Test
  public void testRecursive() throws IOException, NotFoundException {
    BrowseCommandRequest request = new BrowseCommandRequest();

    request.setRecursive(true);

    FileObject root = createCommand().getBrowserResult(request).getFile();
    assertNotNull(root);


    List<FileObject> foList = root.getChildren();

    assertNotNull(foList);
    assertFalse(foList.isEmpty());

    assertEquals(4, foList.size());

    assertEquals("a.txt", foList.get(0).getName());
    assertEquals("b.txt", foList.get(1).getName());
    FileObject c = foList.get(2);
    assertEquals("c", c.getName());
    assertEquals("f.txt", foList.get(3).getName());

    List<FileObject> cChilds = c.getChildren();
    assertEquals("d.txt", cChilds.get(0).getName());
    assertEquals("e.txt", cChilds.get(1).getName());
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private GitBrowseCommand createCommand()
  {
    return new GitBrowseCommand(createContext(), repository);
  }
}
