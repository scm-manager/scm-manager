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

package sonia.scm.web;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitLfsObjectApiDetectorTest {

  @Mock
  private HttpServletRequest request;

  @Test
  void shouldAcceptObjectRequest() {
    when(request.getRequestURI())
      .thenReturn("/scm/repo/scmadmin/lfs.git/info/lfs/objects/abc123");

    boolean result = new GitLfsObjectApiDetector().isScmClient(request, null);

    assertThat(result).isTrue();
  }

  @Test
  void shouldRejectFakeObjectRequest() {
    when(request.getRequestURI())
      .thenReturn("/scm/repo/scmadmin/lfs.git/code/info/lfs/objects/abc123");

    boolean result = new GitLfsObjectApiDetector().isScmClient(request, null);

    assertThat(result).isFalse();
  }
}
