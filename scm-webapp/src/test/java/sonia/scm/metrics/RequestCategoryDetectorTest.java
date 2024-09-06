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

package sonia.scm.metrics;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestCategoryDetectorTest {

  @Mock
  private UserAgentParser userAgentParser;

  @InjectMocks
  private RequestCategoryDetector detector;

  @Test
  void shouldReturnStatic() {
    assertThat(category("/assets/bla")).isEqualTo(RequestCategory.STATIC);
    assertThat(category("/assets/bla/foo/bar")).isEqualTo(RequestCategory.STATIC);
    assertThat(category("/some/path.jpg")).isEqualTo(RequestCategory.STATIC);
    assertThat(category("/some/path.css")).isEqualTo(RequestCategory.STATIC);
    assertThat(category("/some/path.js")).isEqualTo(RequestCategory.STATIC);
    assertThat(category("/my.png")).isEqualTo(RequestCategory.STATIC);
    assertThat(category("/images/loading.svg")).isEqualTo(RequestCategory.STATIC);
  }

  @Test
  void shouldReturnUi() {
    RequestCategory category = category("/", HttpUtil.HEADER_SCM_CLIENT, HttpUtil.SCM_CLIENT_WUI);
    assertThat(category).isEqualTo(RequestCategory.UI);
  }

  @Test
  void shouldReturnApi() {
    assertThat(category("/api/v2")).isEqualTo(RequestCategory.API);
  }

  @Test
  void shouldReturnProtocol() {
    HttpServletRequest request = request("/repo/my/repo");
    when(userAgentParser.parse(request)).thenReturn(UserAgent.scmClient("MySCM").build());
    assertThat(detector.detect(request)).isEqualTo(RequestCategory.PROTOCOL);
  }

  @Test
  void shouldReturnUnknown() {
    assertThat(category("/unknown")).isEqualTo(RequestCategory.UNKNOWN);
  }

  private RequestCategory category(String uri) {
    HttpServletRequest request = request(uri);
    return detector.detect(request);
  }

  private HttpServletRequest request(String uri) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getRequestURI()).thenReturn("/scm" + uri);
    return request;
  }

  private RequestCategory category(String uri, String header, String value) {
    HttpServletRequest request = request(uri);
    when(request.getHeader(header)).thenReturn(value);
    return detector.detect(request);
  }
}
