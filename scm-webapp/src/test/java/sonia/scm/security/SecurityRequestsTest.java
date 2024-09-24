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
