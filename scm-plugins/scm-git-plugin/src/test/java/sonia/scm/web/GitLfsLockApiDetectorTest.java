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

package sonia.scm.web;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitLfsLockApiDetectorTest {

  private final HttpServletRequest request = mock(HttpServletRequest.class);

  private static Stream<Arguments> testParameters() {
    return Stream.of(
      Arguments.of("text/html, image/gif, image/jpeg", false),
      Arguments.of("text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2", false),
      Arguments.of("*", false),
      Arguments.of("application/vnd.git-lfs+json; charset=utf-8", true),
      Arguments.of("application/vnd.git-lfs+json", true)
    );
  }

  @ParameterizedTest
  @MethodSource("testParameters")
  void shouldHandleContentTypeHeaderCorrectly(String headerValue, boolean expected) {
    when(request.getHeader("Content-Type"))
      .thenReturn(headerValue);

    boolean result = new GitLfsLockApiDetector().isScmClient(request, null);

    assertThat(result).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("testParameters")
  void shouldHandleAcceptHeaderCorrectly(String headerValue, boolean expected) {
    when(request.getHeader("Content-Type"))
      .thenReturn(null);
    when(request.getHeader("Accept"))
      .thenReturn(headerValue);

    boolean result = new GitLfsLockApiDetector().isScmClient(request, null);

    assertThat(result).isEqualTo(expected);
  }
}
