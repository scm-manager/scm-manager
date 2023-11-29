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

package sonia.scm.api.v2.resources;

import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.HgGlobalConfig;
import sonia.scm.repository.HgRepositoryHandler;
import sonia.scm.web.HgVndMediaType;
import sonia.scm.web.RestDispatcher;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HgGlobalConfigAutoConfigurationResourceTest {

  private final RestDispatcher dispatcher = new RestDispatcher();

  @InjectMocks
  private HgGlobalConfigDtoToHgConfigMapperImpl dtoToConfigMapper;

  @Mock
  private HgRepositoryHandler repositoryHandler;

  @Mock
  private Provider<HgGlobalConfigAutoConfigurationResource> resourceProvider;

  @Mock
  private Provider<HgRepositoryConfigResource> repositoryConfigResource;

  @Mock
  private Subject subject;

  @BeforeEach
  void prepareEnvironment() {
    HgGlobalConfigAutoConfigurationResource resource = new HgGlobalConfigAutoConfigurationResource(dtoToConfigMapper, repositoryHandler);

    when(resourceProvider.get()).thenReturn(resource);
    dispatcher.addSingletonResource(new HgConfigResource(
      null, null, null,
      resourceProvider, repositoryConfigResource
    ));

    ThreadContext.bind(subject);
  }

  @AfterEach
  void tearDown() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldNotChangeConfigButOnlyInstallHg() throws Exception {
    HgGlobalConfig oldConfig = new HgGlobalConfig();
    oldConfig.setEncoding("UTF-16");
    oldConfig.setEnableHttpPostArgs(true);
    oldConfig.setShowRevisionInId(true);
    when(repositoryHandler.getConfig()).thenReturn(oldConfig);
    MockHttpResponse response = put(null);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);

    verify(repositoryHandler).doAutoConfiguration(any(HgGlobalConfig.class));
    verify(repositoryHandler).setConfig(argThat(config -> {
      assertThat(config.isDisabled()).isFalse();
      assertThat(config.getEncoding()).isEqualTo(oldConfig.getEncoding());
      assertThat(config.isEnableHttpPostArgs()).isEqualTo(oldConfig.isEnableHttpPostArgs());
      assertThat(config.isShowRevisionInId()).isEqualTo(oldConfig.isShowRevisionInId());
      return true;
    }));
  }

  @Test
  void shouldNotSetDefaultConfigAndInstallHgWhenNotAuthorized() throws Exception {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:hg");
    MockHttpResponse response = put(null);

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  @Test
  void shouldNotUpdateConfigButOnlyInstallHg() throws Exception {
    when(repositoryHandler.getConfig()).thenReturn(new HgGlobalConfig());
    MockHttpResponse response = put("{\"disabled\":true}");

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_NO_CONTENT);

    verify(repositoryHandler).doAutoConfiguration(any(HgGlobalConfig.class));
    verify(repositoryHandler).setConfig(argThat(config -> {
      assertThat(config.isDisabled()).isFalse();
      return true;
    }));
  }

  @Test
  void shouldNotUpdateConfigAndInstallHgWhenNotAuthorized() throws Exception {
    doThrow(AuthorizationException.class).when(subject).checkPermission("configuration:write:hg");
    MockHttpResponse response = put("{\"disabled\":true}");

    assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FORBIDDEN);
  }

  private MockHttpResponse put(String content) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/" + HgConfigResource.HG_CONFIG_PATH_V2 + "/auto-configuration");

    if (content != null) {
      request
        .contentType(HgVndMediaType.CONFIG)
        .content(content.getBytes());
    }

    MockHttpResponse response = new MockHttpResponse();
    dispatcher.invoke(request, response);
    return response;
  }
}
