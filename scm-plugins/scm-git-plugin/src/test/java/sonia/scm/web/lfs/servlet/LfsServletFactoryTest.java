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
    
package sonia.scm.web.lfs.servlet;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Test;
import sonia.scm.repository.Repository;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LfsServletFactoryTest {

  private static final String NAMESPACE = "space";
  private static final String NAME = "git-lfs-demo";
  private static final Repository REPOSITORY = new Repository("", "GIT", NAMESPACE, NAME);

  @Test
  public void shouldBuildBaseUri() {
    String result = LfsServletFactory.buildBaseUri(REPOSITORY, requestWithUri("git-lfs-demo"));
    assertThat(result, is(equalTo("http://localhost:8081/scm/repo/space/git-lfs-demo.git/info/lfs/objects/")));
  }

  private HttpServletRequest requestWithUri(String repositoryName) {

    HttpServletRequest mockedRequest = mock(HttpServletRequest.class);

    //build from valid live request data
    when(mockedRequest.getRequestURL()).thenReturn(
      new StringBuffer(String.format("http://localhost:8081/scm/repo/%s/info/lfs/objects/batch", repositoryName)));
    when(mockedRequest.getRequestURI()).thenReturn(String.format("/scm/repo/%s/info/lfs/objects/batch", repositoryName));
    when(mockedRequest.getContextPath()).thenReturn("/scm");

    return mockedRequest;
  }
}
