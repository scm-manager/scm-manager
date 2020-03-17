/**
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


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import sonia.scm.util.HttpUtil;

/**
 * Unit tests for {@link GitUtil}.
 * 
 * @author Sebastian Sdorra
 */
public class GitUtilTest
{

  /**
   * Tests {@link GitUtil#checkBranchName(org.eclipse.jgit.lib.Repository, java.lang.String)} with invalid name.
   *
   * @throws IOException
   */
  @Test(expected = IllegalArgumentException.class)
  public void testCheckInvalidBranchNames() throws IOException
  {
    org.eclipse.jgit.lib.Repository repo = mockRepo(new File("/tmp/test"));

    GitUtil.checkBranchName(repo,
      GitUtil.REF_HEAD_PREFIX.concat("../../../../../etc/passwd"));
  }

  /**
   * Tests {@link GitUtil#checkBranchName(org.eclipse.jgit.lib.Repository, java.lang.String)}.
   *
   * @throws IOException
   */
  @Test
  public void testCheckValidBranchNames() throws IOException
  {
    org.eclipse.jgit.lib.Repository repo = mockRepo(new File("/tmp/test"));

    GitUtil.checkBranchName(repo, GitUtil.REF_HEAD_PREFIX.concat("master"));
    GitUtil.checkBranchName(repo, GitUtil.REF_HEAD_PREFIX.concat("dev"));
    GitUtil.checkBranchName(repo, GitUtil.REF_HEAD_PREFIX.concat("develop"));
  }

  /**
   * Tests {@link GitUtil#getTagName(java.lang.String)}.
   */
  @Test
  public void testGetTagName(){
    assertNull(GitUtil.getTagName("refs/head/master"));
    assertEquals("1.0.0", GitUtil.getTagName("refs/tags/1.0.0"));
    assertEquals("super/1.0.0", GitUtil.getTagName("refs/tags/super/1.0.0"));
  }
  
  /**
   * Tests {@link GitUtil#isBranch(java.lang.String)}.
   */
  @Test
  public void testIsBranchName(){
    assertTrue(GitUtil.isBranch("refs/heads/master"));
    assertTrue(GitUtil.isBranch("refs/heads/feature/super"));
    assertFalse(GitUtil.isBranch(""));
    assertFalse(GitUtil.isBranch(null));
    assertFalse(GitUtil.isBranch("refs/tags/1.0.0"));
    assertFalse(GitUtil.isBranch("refs/heads"));
  }

  private org.eclipse.jgit.lib.Repository mockRepo(File directory)
  {
    org.eclipse.jgit.lib.Repository repo =
      mock(org.eclipse.jgit.lib.Repository.class);

    when(repo.getDirectory()).thenReturn(directory);

    return repo;
  }

  /** Field description */
  @Rule
  public TemporaryFolder temp = new TemporaryFolder();
  
  @Test
  public void testIsGitClient() {
    HttpServletRequest request = mockRequestWithUserAgent("Git/2.9.3");
    assertTrue(GitUtil.isGitClient(request));
    
    request = mockRequestWithUserAgent("JGit/2.9.3");
    assertTrue(GitUtil.isGitClient(request));
    
    request = mockRequestWithUserAgent("Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) ...");
    assertFalse(GitUtil.isGitClient(request));
  }
  
  private HttpServletRequest mockRequestWithUserAgent(String userAgent) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader(HttpUtil.HEADER_USERAGENT)).thenReturn(userAgent);    
    return request;
  }
}
