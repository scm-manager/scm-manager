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
package sonia.scm.web;

import org.junit.Test;

import static org.eclipse.jgit.lfs.lib.Constants.CONTENT_TYPE_GIT_LFS_JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by omilke on 11.05.2017.
 */
public class ScmGitServletTest {

  @Test
  public void isContentTypeMatches() throws Exception {

    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs+json", CONTENT_TYPE_GIT_LFS_JSON), is(true));
    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs+json;", CONTENT_TYPE_GIT_LFS_JSON), is(true));
    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs+json; charset=utf-8", CONTENT_TYPE_GIT_LFS_JSON), is(true));

    assertThat(ScmGitServlet.isLfsContentHeaderField("application/vnd.git-lfs-json;", CONTENT_TYPE_GIT_LFS_JSON), is(false));
    assertThat(ScmGitServlet.isLfsContentHeaderField("", CONTENT_TYPE_GIT_LFS_JSON), is(false));
    assertThat(ScmGitServlet.isLfsContentHeaderField(null, CONTENT_TYPE_GIT_LFS_JSON), is(false));
  }

}
