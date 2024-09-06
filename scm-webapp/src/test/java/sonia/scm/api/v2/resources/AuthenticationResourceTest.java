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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.event.ScmEventBus;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.DefaultAccessTokenCookieIssuer;
import sonia.scm.security.LogoutEvent;
import sonia.scm.web.RestDispatcher;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static java.net.URI.create;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SubjectAware(
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final RestDispatcher dispatcher = new RestDispatcher();

  @Mock
  private AccessTokenBuilderFactory accessTokenBuilderFactory;

  @Mock
  private AccessTokenBuilder accessTokenBuilder;

  @Mock
  private ScmEventBus eventBus;

  private MeterRegistry meterRegistry;

  private final AccessTokenCookieIssuer cookieIssuer = new DefaultAccessTokenCookieIssuer(mock(ScmConfiguration.class));

  private final MockHttpResponse response = new MockHttpResponse();

  private static final String AUTH_JSON_TRILLIAN = "{\n" +
    "\t\"cookie\": true,\n" +
    "\t\"grant_type\": \"password\",\n" +
    "\t\"username\": \"trillian\",\n" +
    "\t\"password\": \"secret\"\n" +
    "}";

  private static final String AUTH_FORMENCODED_TRILLIAN = "cookie=true&grant_type=password&username=trillian&password=secret";

  private static final String AUTH_JSON_TRILLIAN_WRONG_PW = "{\n" +
    "\t\"cookie\": true,\n" +
    "\t\"grant_type\": \"password\",\n" +
    "\t\"username\": \"trillian\",\n" +
    "\t\"password\": \"justWrong\"\n" +
    "}";

  private static final String AUTH_JSON_NOT_EXISTING_USER = "{\n" +
    "\t\"cookie\": true,\n" +
    "\t\"grant_type\": \"password\",\n" +
    "\t\"username\": \"iDoNotExist\",\n" +
    "\t\"password\": \"doesNotMatter\"\n" +
    "}";

  private static final String AUTH_JSON_WITHOUT_USERNAME = String.join("\n",
    "{",
    "\"grant_type\": \"password\",",
    "\"password\": \"tricia123\"",
    "}"
  );

  private static final String AUTH_JSON_WITHOUT_PASSWORD = String.join("\n",
    "{",
    "\"grant_type\": \"password\",",
    "\"username\": \"trillian\"",
    "}"
  );

  private static final String AUTH_JSON_WITHOUT_GRANT_TYPE = String.join("\n",
    "{",
    "\"username\": \"trillian\",",
    "\"password\": \"tricia123\"",
    "}"
  );

  private static final String AUTH_JSON_WITH_INVALID_GRANT_TYPE = String.join("\n",
    "{",
    "\"grant_type\": \"el speciale\",",
    "\"username\": \"trillian\",",
    "\"password\": \"tricia123\"",
    "}"
  );

  private AuthenticationResource authenticationResource;

  @Before
  public void prepareEnvironment() {
    meterRegistry = new SimpleMeterRegistry();
    authenticationResource = new AuthenticationResource(accessTokenBuilderFactory, cookieIssuer, meterRegistry, eventBus);
    dispatcher.addSingletonResource(authenticationResource);

    AccessToken accessToken = mock(AccessToken.class);
    when(accessTokenBuilder.build()).thenReturn(accessToken);

    when(accessTokenBuilderFactory.create()).thenReturn(accessTokenBuilder);

    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    dispatcher.putDefaultContextObject(HttpServletRequest.class, servletRequest);
    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    dispatcher.putDefaultContextObject(HttpServletResponse.class, servletResponse);
  }

  @Test
  public void shouldAuthCorrectly() throws URISyntaxException {

    MockHttpRequest request = getMockHttpRequest(AUTH_JSON_TRILLIAN);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    List<Meter> meters = meterRegistry.getMeters();
    assertThat(meters).hasSize(3);
    Optional<Meter> loginAttemptMeter = meters.stream().filter(m -> m.getId().getName().equals("scm.auth.login.attempts")).findFirst();
    assertThat(loginAttemptMeter).isPresent();
    assertThat(loginAttemptMeter.get().measure().iterator().next().getValue()).isEqualTo(1);
  }

  @Test
  public void shouldAuthCorrectlyWithFormencodedData() throws URISyntaxException {

    MockHttpRequest request = getMockHttpRequestUrlEncoded(AUTH_FORMENCODED_TRILLIAN);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());

    List<Meter> meters = meterRegistry.getMeters();
    assertThat(meters).hasSize(3);
    Optional<Meter> loginAttemptMeter = meters.stream().filter(m -> m.getId().getName().equals("scm.auth.login.attempts")).findFirst();
    assertThat(loginAttemptMeter).isPresent();
    assertThat(loginAttemptMeter.get().measure().iterator().next().getValue()).isEqualTo(1);
  }


  @Test
  public void shouldNotAuthUserWithWrongPassword() throws URISyntaxException {

    MockHttpRequest request = getMockHttpRequest(AUTH_JSON_TRILLIAN_WRONG_PW);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

    List<Meter> meters = meterRegistry.getMeters();
    assertThat(meters).hasSize(3);
    assertThat(meters.stream().map(m -> m.getId().getName())).contains("scm.auth.login.failed", "scm.auth.login.attempts", "scm.auth.logout");
  }

  @Test
  public void shouldNotAuthNonexistingUser() throws URISyntaxException {
    MockHttpRequest request = getMockHttpRequest(AUTH_JSON_NOT_EXISTING_USER);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());

    List<Meter> meters = meterRegistry.getMeters();
    assertThat(meters).hasSize(3);
    assertThat(meters.stream().map(m -> m.getId().getName())).contains("scm.auth.login.failed", "scm.auth.login.attempts", "scm.auth.logout");
  }

  @Test
  public void shouldReturnBadStatusIfPasswordParameterIsMissing() throws URISyntaxException {
    shouldReturnBadRequest(AUTH_JSON_WITHOUT_USERNAME);
  }

  @Test
  public void shouldReturnBadStatusIfUsernameParameterIsMissing() throws URISyntaxException {
    shouldReturnBadRequest(AUTH_JSON_WITHOUT_PASSWORD);
  }

  @Test
  public void shouldReturnBadStatusIfGrantTypeParameterIsMissing() throws URISyntaxException {
    shouldReturnBadRequest(AUTH_JSON_WITHOUT_GRANT_TYPE);
  }

  @Test
  public void shouldReturnBadStatusIfGrantTypeParameterIsInvalid() throws URISyntaxException {
    shouldReturnBadRequest(AUTH_JSON_WITH_INVALID_GRANT_TYPE);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldSuccessfullyLogoutUser() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/" + AuthenticationResource.PATH + "/access_token");

    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());

    List<Meter> meters = meterRegistry.getMeters();
    assertThat( meters).hasSize(3);
    Optional<Meter> logoutMeter = meters.stream().filter(m -> m.getId().getName().equals("scm.auth.logout")).findFirst();
    assertThat(logoutMeter).isPresent();
    assertThat(logoutMeter.get().measure().iterator().next().getValue()).isEqualTo(1);

    verify(eventBus).post(any(LogoutEvent.class));
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldFireLogoutEvent() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/" + AuthenticationResource.PATH + "/access_token");

    dispatcher.invoke(request, response);
    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());

    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    verify(eventBus).post(captor.capture());

    Object event = captor.getValue();
    assertThat(event).isInstanceOfSatisfying(LogoutEvent.class, logoutEvent -> {
      assertThat(logoutEvent.getPrimaryPrincipal()).isEqualTo("trillian");
    });
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldHandleLogoutRedirection() throws URISyntaxException, UnsupportedEncodingException {
    authenticationResource.setLogoutRedirection(() -> of(create("http://example.com/cas/logout")));

    MockHttpRequest request = MockHttpRequest.delete("/" + AuthenticationResource.PATH + "/access_token");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertThat(response.getContentAsString()).contains("http://example.com/cas/logout");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldHandleDisabledLogoutRedirection() throws URISyntaxException {
    authenticationResource.setLogoutRedirection(Optional::empty);

    MockHttpRequest request = MockHttpRequest.delete("/" + AuthenticationResource.PATH + "/access_token");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  private void shouldReturnBadRequest(String requestBody) throws URISyntaxException {
    MockHttpRequest request = getMockHttpRequest(requestBody);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  private MockHttpRequest getMockHttpRequest(String jsonPayload) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.post("/" + AuthenticationResource.PATH + "/access_token");

    request.content(jsonPayload.getBytes());
    request.contentType(MediaType.APPLICATION_JSON_TYPE);
    return request;
  }

  private MockHttpRequest getMockHttpRequestUrlEncoded(String payload) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.post("/" + AuthenticationResource.PATH + "/access_token");

    request.content(payload.getBytes());
    request.contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    return request;
  }
}
