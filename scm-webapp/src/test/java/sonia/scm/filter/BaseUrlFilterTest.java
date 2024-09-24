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

package sonia.scm.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;



@ExtendWith(MockitoExtension.class)
class BaseUrlFilterTest {


  @Mock
  private HttpServletResponse response;

  @Mock
  private FilterChain chain;

  private ScmConfiguration configuration;

  private BaseUrlFilter filter;

  @BeforeEach
  void setUpFilter() {
    configuration = new ScmConfiguration();
    filter = new BaseUrlFilter(configuration);
  }

  @Test
  void shouldSetBaseUrl() throws ServletException, IOException {
    HttpServletRequest request = mockRequest("https", "hitchhiker.com", 443, "/scm");

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
    assertThat(configuration.getBaseUrl()).isEqualTo("https://hitchhiker.com:443/scm");
  }

  @Test
  void shouldSendRedirect() throws ServletException, IOException {
    configuration.setBaseUrl("https://hitchhiker.com:443/scm");
    configuration.setForceBaseUrl(true);
    HttpServletRequest request = mockRequest("http://192.168.1.42:8081", "/scm", "/api/v2");

    filter.doFilter(request, response, chain);

    verifyNoInteractions(chain);
    verify(response).setStatus(307);
    verify(response).setHeader("Location", "https://hitchhiker.com:443/scm/api/v2");
  }

  @Test
  void shouldNotSendRedirectIfDisabled() throws ServletException, IOException {
    configuration.setBaseUrl("https://hitchhiker.com:443/scm");
    configuration.setForceBaseUrl(false);
    HttpServletRequest request = mockRequest("http://192.168.1.42:8081", "/scm", "/api/v2");

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  @Test
  void shouldNotSendRedirect() throws ServletException, IOException {
    configuration.setBaseUrl("https://hitchhiker.com:443/scm");
    configuration.setForceBaseUrl(true);
    HttpServletRequest request = mockRequest("https://hitchhiker.com", "/scm", "/api/v2/users");

    filter.doFilter(request, response, chain);

    verify(chain).doFilter(request, response);
  }

  private HttpServletRequest mockRequest(String baseUrl, String contextPath, String path) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    lenient().when(request.getRequestURL()).thenReturn(new StringBuffer(baseUrl).append(contextPath).append(path));
    lenient().when(request.getContextPath()).thenReturn(contextPath);
    lenient().when(request.getRequestURI()).thenReturn(contextPath + path);
    return request;
  }

  private HttpServletRequest mockRequest(String scheme, String serverName, int port, String contextPath) {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getScheme()).thenReturn(scheme);
    when(request.getServerName()).thenReturn(serverName);
    when(request.getServerPort()).thenReturn(port);
    when(request.getContextPath()).thenReturn(contextPath);
    return request;
  }

  @Nested
  class StartsWithTests {

    @Test
    void shouldReturnTrue() {
      assertThat(
        filter.startsWith("http://www.scm-manager.org/scm", "http://www.scm-manager.org/scm")
      ).isTrue();
      assertThat(
        filter.startsWith("http://www.scm-manager.org:80/scm", "http://www.scm-manager.org/scm")
      ).isTrue();
      assertThat(
        filter.startsWith("https://www.scm-manager.org/scm", "https://www.scm-manager.org:443/scm")
      ).isTrue();
    }

    @Test
    void shouldReturnFalse() {
      assertThat(
        filter.startsWith("http://www.scm-manager.org/acb", "http://www.scm-manager.org/scm")
      ).isFalse();
    }

  }
}
