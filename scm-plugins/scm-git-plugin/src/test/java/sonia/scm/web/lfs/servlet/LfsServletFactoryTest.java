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
