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

package sonia.scm;

import com.google.inject.OutOfScopeException;
import com.google.inject.ProvisionException;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;

import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultRootURLTest {

  private static final String URL_CONFIG = "https://hitchhiker.com/from-configuration";
  private static final String URL_REQUEST = "https://hitchhiker.com/from-request";

  @Mock
  private Provider<HttpServletRequest> requestProvider;

  @Mock
  private HttpServletRequest request;

  private ScmConfiguration configuration;

  private DefaultRootURL rootURL;
  @Spy
  private DefaultRootURL.UrlFromString cacheLoader;

  @BeforeEach
  void init() {
    configuration = new ScmConfiguration();
    rootURL = new DefaultRootURL(requestProvider, configuration, cacheLoader);
  }

  @Test
  void shouldUseRootURLFromRequest() {
    bindRequestUrl();
    assertThat(rootURL.getAsString()).isEqualTo(URL_REQUEST);
  }

  private void bindRequestUrl() {
    when(requestProvider.get()).thenReturn(request);
    when(request.getRequestURL()).thenReturn(new StringBuffer(URL_REQUEST));
    when(request.getRequestURI()).thenReturn("/from-request");
    when(request.getContextPath()).thenReturn("/from-request");
  }

  @Test
  void shouldUseRootURLFromConfiguration() {
    bindNonHttpScope();
    configuration.setBaseUrl(URL_CONFIG);
    assertThat(rootURL.getAsString()).isEqualTo(URL_CONFIG);
  }

  @Test
  void shouldSuppressDefaultPorts() {
    bindNonHttpScope();
    configuration.setBaseUrl("https://hitchhiker.com:443/from-configuration");
    assertThat(rootURL.getAsString()).isEqualTo(URL_CONFIG);
  }

  private void bindNonHttpScope() {
    when(requestProvider.get()).thenThrow(
      new ProvisionException("no request available", new OutOfScopeException("out of scope"))
    );
  }

  @Test
  void shouldThrowNonOutOfScopeProvisioningExceptions() {
    when(requestProvider.get()).thenThrow(
      new ProvisionException("something ugly happened", new IllegalStateException("some wrong state"))
    );

    assertThrows(ProvisionException.class, () -> rootURL.get());
  }

  @Test
  void shouldThrowIllegalStateExceptionForMalformedBaseUrl() {
    bindNonHttpScope();
    configuration.setBaseUrl("non_url");

    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> rootURL.get());
    assertThat(exception.getMessage()).contains("malformed", "non_url");
  }

  @Test
  void shouldThrowIllegalStateExceptionIfBaseURLIsNotConfigured() {
    bindNonHttpScope();

    IllegalStateException exception = assertThrows(IllegalStateException.class, () -> rootURL.get());
    assertThat(exception.getMessage()).contains("empty");
  }

  @Test
  void shouldUseRootURLFromForwardedRequest() {
    bindForwardedRequestUrl();
    assertThat(rootURL.get()).hasHost("hitchhiker.com");
  }

  @Test
  void shouldUseUrlCache() throws MalformedURLException {
    bindForwardedRequestUrl();

    rootURL.get();
    rootURL.get();

    verify(cacheLoader).load(any());
  }

  private void bindForwardedRequestUrl() {
    when(requestProvider.get()).thenReturn(request);
    when(request.getHeader(HttpUtil.HEADER_X_FORWARDED_HOST)).thenReturn("hitchhiker.com");
    when(request.getScheme()).thenReturn("https");
    when(request.getServerPort()).thenReturn(443);
    when(request.getContextPath()).thenReturn("/from-request");
  }

}
