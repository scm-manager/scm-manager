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
