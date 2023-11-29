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

package sonia.scm.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Created by masuewer on 04.07.18.
 */
@ExtendWith(MockitoExtension.class)
class SecurityRequestsTest {

  @Mock
  private HttpServletRequest request;

  @Test
  void shouldReturnTrueWithContextPath() {
    when(request.getRequestURI()).thenReturn("/scm/api/auth/access_token");
    when(request.getContextPath()).thenReturn("/scm");

    assertThat(SecurityRequests.isAuthenticationRequest(request)).isTrue();
  }

  @Test
  void shouldDetectAuthenticationResource() {
    assertThat(SecurityRequests.isAuthenticationRequest("/api/auth/access_token")).isTrue();
    assertThat(SecurityRequests.isAuthenticationRequest("/api/v2/auth/access_token")).isTrue();
    assertThat(SecurityRequests.isAuthenticationRequest("/api/repositories")).isFalse();
    assertThat(SecurityRequests.isAuthenticationRequest("/api/v2/repositories")).isFalse();
  }

  @Test
  void shouldReturnFalseForLogout() {
    when(request.getRequestURI()).thenReturn("/scm/api/auth/access_token");
    when(request.getContextPath()).thenReturn("/scm");
    when(request.getMethod()).thenReturn("DELETE");

    assertThat(SecurityRequests.isAuthenticationRequest(request)).isFalse();
  }

}
