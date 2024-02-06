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

import com.google.inject.OutOfScopeException;
import com.google.inject.ProvisionException;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link XsrfAccessTokenEnricher}.
 *
 */
@ExtendWith(MockitoExtension.class)
class XsrfAccessTokenEnricherTest {

  @Mock
  private HttpServletRequest request;

  @Mock
  private AccessTokenBuilder builder;

  private ScmConfiguration configuration;

  private XsrfAccessTokenEnricher enricher;

  @BeforeEach
  void createConfiguration() {
    configuration = new ScmConfiguration();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testWithoutRequestScope() {
    // prepare
    Provider<HttpServletRequest> requestProvider = mock(Provider.class);
    when(requestProvider.get()).thenThrow(new ProvisionException("failed to provision", new OutOfScopeException("no request scope is available")));
    configuration.setEnabledXsrfProtection(true);
    XsrfAccessTokenEnricher enricher = createEnricher(requestProvider);

    // execute
    enricher.enrich(builder);

    // assert
    verify(builder, never()).custom(Xsrf.TOKEN_KEY, "42");
  }

  @Test
  @SuppressWarnings("unchecked")
  void testWithProvisionException() {
    // prepare
    Provider<HttpServletRequest> requestProvider = mock(Provider.class);
    when(requestProvider.get()).thenThrow(new ProvisionException("failed to provision"));
    configuration.setEnabledXsrfProtection(true);
    XsrfAccessTokenEnricher enricher = createEnricher(requestProvider);

    // execute
    assertThrows(ProvisionException.class, () -> enricher.enrich(builder));
  }

  private XsrfAccessTokenEnricher createEnricher(Provider<HttpServletRequest> requestProvider) {
    return new XsrfAccessTokenEnricher(configuration, requestProvider) {
      @Override
      String createToken() {
        return "42";
      }
    };
  }

  @Nested
  class WithRequestMock {

    @BeforeEach
    void setupEnricher() {
      enricher = createEnricher(() -> request);
    }

    @Test
    void testEnrich() {
      // prepare
      configuration.setEnabledXsrfProtection(true);
      when(request.getHeader(HttpUtil.HEADER_SCM_CLIENT)).thenReturn(HttpUtil.SCM_CLIENT_WUI);

      // execute
      enricher.enrich(builder);

      // assert
      verify(builder).custom(Xsrf.TOKEN_KEY, "42");
    }

    @Test
    void testEnrichWithDisabledXsrf() {
      // prepare
      configuration.setEnabledXsrfProtection(false);

      // execute
      enricher.enrich(builder);

      // assert
      verify(builder, never()).custom(Xsrf.TOKEN_KEY, "42");
    }

    @Test
    void testEnrichWithNonWuiClient() {
      // prepare
      configuration.setEnabledXsrfProtection(true);

      // execute
      enricher.enrich(builder);

      // assert
      verify(builder, never()).custom(Xsrf.TOKEN_KEY, "42");
    }
  }
}
