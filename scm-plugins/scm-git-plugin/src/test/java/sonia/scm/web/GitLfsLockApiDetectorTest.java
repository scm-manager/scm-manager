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
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GitLfsLockApiDetectorTest {

  @Mock
  private HttpServletRequest request;

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
