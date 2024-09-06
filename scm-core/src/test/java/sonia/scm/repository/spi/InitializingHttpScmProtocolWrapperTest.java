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

package sonia.scm.repository.spi;

import com.google.inject.ProvisionException;
import com.google.inject.util.Providers;
import jakarta.inject.Provider;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;
import sonia.scm.RootURL;
import sonia.scm.api.v2.resources.ScmPathInfo;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.repository.Repository;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitializingHttpScmProtocolWrapperTest {

  private static final Repository REPOSITORY = new Repository("", "git", "space", "name");

  @Mock
  private ScmProviderHttpServlet delegateServlet;
  private InitializingHttpScmProtocolWrapper wrapper;


  @Nested
  class WithRootURL {

    @Mock
    private RootURL rootURL;

    @BeforeEach
    void init() {
      wrapper = new InitializingHttpScmProtocolWrapper(Providers.of(delegateServlet), rootURL) {
        @Override
        public String getType() {
          return "git";
        }
      };
      when(rootURL.getAsString()).thenReturn("https://hitchhiker.com/scm");
    }

    @Test
    void shouldReturnUrlFromRootURL() {
      HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

      assertEquals("https://hitchhiker.com/scm/repo/space/name", httpScmProtocol.getUrl());
    }

  }

  @Nested
  class WithPathInfoStore {

    @Mock
    private ScmPathInfoStore pathInfoStore;
    @Mock
    private ScmConfiguration scmConfiguration;

    private Provider<ScmPathInfoStore> pathInfoStoreProvider;

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private ServletConfig servletConfig;

    @BeforeEach
    void init() {
      pathInfoStoreProvider = mock(Provider.class);
      lenient().when(pathInfoStoreProvider.get()).thenReturn(pathInfoStore);

      wrapper = new InitializingHttpScmProtocolWrapper(Providers.of(delegateServlet), pathInfoStoreProvider, scmConfiguration) {
        @Override
        public String getType() {
          return "git";
        }
      };
      lenient().when(scmConfiguration.getBaseUrl()).thenReturn("http://example.com/scm");
    }

    @Test
    void shouldUsePathFromPathInfo() {
      mockSetPathInfo();

      HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

      assertEquals("http://example.com/scm/repo/space/name", httpScmProtocol.getUrl());
    }

    @Test
    void shouldUseConfigurationWhenPathInfoNotSet() {
      HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

      assertEquals("http://example.com/scm/repo/space/name", httpScmProtocol.getUrl());
    }

    @Test
    void shouldUseConfigurationWhenNotInRequestScope() {
      when(pathInfoStoreProvider.get()).thenThrow(new ProvisionException("test"));

      HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

      assertEquals("http://example.com/scm/repo/space/name", httpScmProtocol.getUrl());
    }

    @Test
    void shouldInitializeAndDelegateRequestThroughFilter() throws ServletException, IOException {
      HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

      httpScmProtocol.serve(request, response, servletConfig);

      verify(delegateServlet).init(servletConfig);
      verify(delegateServlet).service(request, response, REPOSITORY);
    }

    @Test
    void shouldInitializeOnlyOnce() throws ServletException, IOException {
      HttpScmProtocol httpScmProtocol = wrapper.get(REPOSITORY);

      httpScmProtocol.serve(request, response, servletConfig);
      httpScmProtocol.serve(request, response, servletConfig);

      verify(delegateServlet, times(1)).init(servletConfig);
      verify(delegateServlet, times(2)).service(request, response, REPOSITORY);
    }

    @Test
    void shouldFailForIllegalScmType() {
      Repository repository = new Repository("", "other", "space", "name");
      assertThrows(
        IllegalArgumentException.class,
        () -> wrapper.get(repository)
      );
    }

    private OngoingStubbing<ScmPathInfo> mockSetPathInfo() {
      return when(pathInfoStore.get()).thenReturn(() -> URI.create("http://example.com/scm/api/"));
    }

  }

}
