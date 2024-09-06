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

package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.SubjectAware;
import jakarta.inject.Provider;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.initialization.InitializationAuthenticationService;
import sonia.scm.lifecycle.AdminAccountStartupAction;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.DefaultAccessTokenCookieIssuer;
import sonia.scm.web.RestDispatcher;

import java.net.URISyntaxException;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.resteasy.mock.MockHttpRequest.post;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SubjectAware(
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@ExtendWith(MockitoExtension.class)
class AdminAccountStartupResourceTest {

  private final RestDispatcher dispatcher = new RestDispatcher();
  private final MockHttpResponse response = new MockHttpResponse();

  @Mock
  private AdminAccountStartupAction startupAction;
  @Mock
  private Provider<ScmPathInfoStore> pathInfoStoreProvider;
  @Mock
  private InitializationAuthenticationService authenticationService;
  @Mock
  private ScmPathInfoStore pathInfoStore;
  @Mock
  private ScmPathInfo pathInfo;
  @Mock
  private AccessTokenBuilderFactory accessTokenBuilderFactory;
  @Mock
  private AccessTokenBuilder accessTokenBuilder;
  private final AccessTokenCookieIssuer cookieIssuer = new DefaultAccessTokenCookieIssuer(mock(ScmConfiguration.class));

  @InjectMocks
  private AdminAccountStartupResource resource;

  @BeforeEach
  void setUpMocks() {
    lenient().when(pathInfoStoreProvider.get()).thenReturn(pathInfoStore);
    lenient().when(pathInfoStore.get()).thenReturn(pathInfo);
    dispatcher.addSingletonResource(new InitializationResource(singleton(resource)));
    lenient().when(startupAction.name()).thenReturn("adminAccount");
    lenient().when(accessTokenBuilderFactory.create()).thenReturn(accessTokenBuilder);
    AccessToken accessToken = mock(AccessToken.class);
    lenient().when(accessTokenBuilder.build()).thenReturn(accessToken);
  }

  @Test
  void shouldFailWhenActionIsDone() throws URISyntaxException {
    when(startupAction.done()).thenReturn(true);

    MockHttpRequest request =
      post("/v2/initialization/adminAccount")
        .contentType("application/json")
        .content(createInput("irrelevant", "irrelevant", "irrelevant", "irrelevant@some.com", "irrelevant", "irrelevant"));
    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Nested
  class WithNecessaryAction {

    @BeforeEach
    void actionNotDone() {
      when(startupAction.done()).thenReturn(false);
      when(startupAction.isCorrectToken(any())).thenAnswer(i -> "initial-token".equals(i.getArgument(0)));
    }

    @Test
    void shouldFailWithWrongToken() throws URISyntaxException {
      MockHttpRequest request =
        post("/v2/initialization/adminAccount")
          .contentType("application/json")
          .content(createInput("wrong-token", "trillian", "Tricia", "tricia@hitchhiker.com", "something", "different"));
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void shouldFailWhenPasswordsAreNotEqual() throws URISyntaxException {
      MockHttpRequest request =
        post("/v2/initialization/adminAccount")
          .contentType("application/json")
          .content(createInput("initial-token", "trillian", "Tricia", "tricia@hitchhiker.com", "something", "different"));
      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void shouldCreateAdminUser() throws URISyntaxException {
      Subject subject = mock(Subject.class);
      ThreadContext.bind(subject);

      MockHttpRequest request =
        post("/v2/initialization/adminAccount")
          .contentType("application/json")
          .content(createInput("initial-token", "trillian", "Tricia", "tricia@hitchhiker.com", "password", "password"));

      HttpServletRequest servletRequest = mock(HttpServletRequest.class);
      dispatcher.putDefaultContextObject(HttpServletRequest.class, servletRequest);

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(204);

      verify(startupAction).createAdminUser("trillian", "Tricia", "tricia@hitchhiker.com", "password");
    }
  }

  private byte[] createInput(String token, String userName, String displayName, String email, String password, String confirmation) {
    return json(format("{'startupToken': '%s', 'userName': '%s', 'displayName': '%s', 'email': '%s', 'password': '%s', 'passwordConfirmation': '%s'}", token, userName, displayName, email, password, confirmation));
  }

  private byte[] json(String s) {
    return s.replaceAll("'", "\"").getBytes(UTF_8);
  }
}
