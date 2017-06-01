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


package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.junit.Test;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
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
