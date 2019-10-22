/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://bitbucket.org/sdorra/scm-manager
 */

package sonia.scm.security;

import com.google.inject.OutOfScopeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.util.HttpUtil;

import javax.inject.Provider;
import javax.servlet.http.HttpServletRequest;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link XsrfAccessTokenEnricher}.
 *
 * @author Sebastian Sdorra
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
    when(requestProvider.get()).thenThrow(new OutOfScopeException("request scope is not available"));
    configuration.setEnabledXsrfProtection(true);
    XsrfAccessTokenEnricher enricher = createEnricher(requestProvider);

    // execute
    enricher.enrich(builder);

    // assert
    verify(builder, never()).custom(Xsrf.TOKEN_KEY, "42");
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
