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

package sonia.scm.web.protocol;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.NotFoundException;
import sonia.scm.PushStateDispatcher;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.DefaultRepositoryProvider;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryTestData;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.repository.spi.HttpScmProtocol;
import sonia.scm.util.HttpUtil;
import sonia.scm.web.ScmClientDetector;
import sonia.scm.web.UserAgent;
import sonia.scm.web.UserAgentParser;

import java.io.IOException;
import java.util.Optional;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("java:S5979") // This seems to be erroneous here (not initialized mocks)
class HttpProtocolServletTest {

  @Mock
  private RepositoryServiceFactory serviceFactory;

  @Mock
  private NamespaceAndNameFromPathExtractor extractor;

  @Mock
  private PushStateDispatcher dispatcher;

  @Mock
  private UserAgentParser userAgentParser;

  @Mock
  private ScmConfiguration configuration;

  private HttpProtocolServlet servlet;

  @Mock
  private RepositoryService repositoryService;

  @Mock
  private UserAgent userAgent;

  @Mock
  private HttpServletRequest request;

  @Mock
  private HttpServletResponse response;

  @Mock
  private HttpScmProtocol protocol;

  @Nested
  class WithoutAdditionalScmClientDetector {

    @BeforeEach
    void initServlet() {
      servlet = new HttpProtocolServlet(
        configuration,
        serviceFactory,
        extractor,
        dispatcher,
        userAgentParser,
        emptySet()
      );
    }

    @Nested
    class Browser {

      @BeforeEach
      void prepareMocks() {
        when(userAgentParser.parse(request)).thenReturn(userAgent);
        when(userAgent.isScmClient()).thenReturn(false);
        when(request.getRequestURI()).thenReturn("uri");
      }

      @Test
      void shouldDispatchBrowserRequests() throws ServletException, IOException {
        servlet.service(request, response);

        verify(dispatcher).dispatch(request, response, "uri");
      }

    }

    @Nested
    class ScmClient {

      @BeforeEach
      void prepareMocks() {
        when(userAgentParser.parse(request)).thenReturn(userAgent);
        when(userAgent.isScmClient()).thenReturn(true);
      }

      @Test
      void shouldHandleBadPaths() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/illegal");

        servlet.service(request, response);

        verify(response).setStatus(400);
      }

      @Test
      void shouldHandleNotExistingRepository() throws IOException, ServletException {
        when(request.getPathInfo()).thenReturn("/not/exists");

        NamespaceAndName repo = new NamespaceAndName("not", "exists");
        when(extractor.fromUri("/not/exists")).thenReturn(Optional.of(repo));
        when(serviceFactory.create(repo)).thenThrow(new NotFoundException("Test", "a"));

        servlet.service(request, response);

        verify(response).setStatus(404);
      }

      @Test
      void shouldDelegateToProvider() throws IOException, ServletException {
        NamespaceAndName repo = new NamespaceAndName("space", "name");
        when(extractor.fromUri("/space/name")).thenReturn(Optional.of(repo));
        when(serviceFactory.create(repo)).thenReturn(repositoryService);

        when(request.getPathInfo()).thenReturn("/space/name");
        Repository repository = RepositoryTestData.createHeartOfGold();
        when(repositoryService.getRepository()).thenReturn(repository);
        when(repositoryService.getProtocol(HttpScmProtocol.class)).thenReturn(protocol);

        servlet.service(request, response);

        verify(request).setAttribute(DefaultRepositoryProvider.ATTRIBUTE_NAME, repository);
        verify(protocol).serve(request, response, null);
        verify(repositoryService).close();
      }

      @Nested
      class WithSubject {

        @Mock
        private Subject subject;

        @BeforeEach
        void setUpSubject() {
          ThreadContext.bind(subject);
        }

        @AfterEach
        void tearDownSubject() {
          ThreadContext.unbindSubject();
        }

        @Test
        void shouldSendUnauthorizedWithCustomRealmDescription() throws IOException, ServletException {
          when(subject.getPrincipal()).thenReturn(SCMContext.USER_ANONYMOUS);
          when(configuration.getRealmDescription()).thenReturn("Hitchhikers finest");

          callServiceWithAuthorizationException();

          verify(response).setHeader(HttpUtil.HEADER_WWW_AUTHENTICATE, "Basic realm=\"Hitchhikers finest\"");
          verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, HttpUtil.STATUS_UNAUTHORIZED_MESSAGE);
        }

        @Test
        void shouldSendForbidden() throws IOException, ServletException {
          callServiceWithAuthorizationException();

          verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        }

        private void callServiceWithAuthorizationException() throws IOException, ServletException {
          NamespaceAndName repo = new NamespaceAndName("space", "name");
          when(extractor.fromUri("/space/name")).thenReturn(Optional.of(repo));
          when(serviceFactory.create(repo)).thenReturn(repositoryService);

          when(request.getPathInfo()).thenReturn("/space/name");
          Repository repository = RepositoryTestData.createHeartOfGold();
          when(repositoryService.getRepository()).thenReturn(repository);
          when(repositoryService.getProtocol(HttpScmProtocol.class)).thenThrow(
            new AuthorizationException("failed")
          );

          servlet.service(request, response);
        }
      }
    }
  }

  @Nested
  class WithAdditionalDetector {

    @Mock
    private ScmClientDetector detector;

    @BeforeEach
    void createServlet() {
      servlet = new HttpProtocolServlet(
        configuration,
        serviceFactory,
        extractor,
        dispatcher,
        userAgentParser,
        singleton(detector)
      );
    }

    @Test
    void shouldConsultScmDetector() throws ServletException, IOException {
      when(userAgentParser.parse(request)).thenReturn(userAgent);
      when(detector.isScmClient(request, userAgent)).thenReturn(true);

      servlet.service(request, response);

      verify(response).setStatus(400);
    }
  }
}
