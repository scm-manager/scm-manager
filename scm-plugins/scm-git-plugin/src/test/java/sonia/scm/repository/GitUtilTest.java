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

package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import org.assertj.core.api.Assertions;
import org.eclipse.jgit.attributes.Attribute;
import org.eclipse.jgit.attributes.Attributes;
import org.eclipse.jgit.lfs.LfsPointer;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectStream;
import org.eclipse.jgit.lib.Repository;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
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

  @Test
  public void testIsGitClient() {
    HttpServletRequest request = mockRequestWithUserAgent("Git/2.9.3");
    assertTrue(GitUtil.isGitClient(request));

    request = mockRequestWithUserAgent("JGit/2.9.3");
    assertTrue(GitUtil.isGitClient(request));

    request = mockRequestWithUserAgent("Mozilla/5.0 (Linux; Android 4.0.4; Galaxy Nexus Build/IMM76B) ...");
    assertFalse(GitUtil.isGitClient(request));
  }

  @Test
  public void testLfsPointerWithRealPointer() throws IOException {
    String lfsPointer = "version https://git-lfs.github.com/spec/v1\n" +
      "oid sha256:e84d872d96a5e6320825968a7745cdaaf6c67c98fab7f480ea6edb2b040a4293\n" +
      "size 6976827\n";

    Optional<LfsPointer> result = callGetLfsPointer(lfsPointer);

    Assertions.assertThat(result).isNotEmpty();
    Assertions.assertThat(result.get().getOid().getName())
      .isEqualTo("e84d872d96a5e6320825968a7745cdaaf6c67c98fab7f480ea6edb2b040a4293");
  }

  @Test
  public void testLfsPointerWithIllegalPointer() throws IOException {
    String lfsPointer = "This is anything, but not an lfs pointer. But it should not raise an exception\n";

    Optional<LfsPointer> result = callGetLfsPointer(lfsPointer);

    Assertions.assertThat(result).isEmpty();
  }

  private Optional<LfsPointer> callGetLfsPointer(String lfsPointer) throws IOException {
    Repository repository = mock(Repository.class);
    ObjectId objectId = new ObjectId(1, 2, 3, 4, 5);
    Attributes attributes = mock(Attributes.class);
    Attribute filter = mock(Attribute.class);
    ObjectLoader objectLoader = mock(ObjectLoader.class);
    when(attributes.get("filter")).thenReturn(filter);
    when(filter.getValue()).thenReturn("lfs");
    when(repository.open(objectId, Constants.OBJ_BLOB)).thenReturn(objectLoader);
    when(objectLoader.openStream()).thenReturn(new ObjectStream.SmallStream(1, lfsPointer.getBytes(UTF_8)));

    Optional<LfsPointer> result = GitUtil.getLfsPointer(repository, objectId, attributes);
    return result;
  }

  private HttpServletRequest mockRequestWithUserAgent(String userAgent) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getHeader(HttpUtil.HEADER_USERAGENT)).thenReturn(userAgent);
    return request;
  }
}
