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

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.security.DefaultAccessTokenCookieIssuer;
import sonia.scm.web.RestDispatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Optional;

import static java.net.URI.create;
import static java.util.Optional.of;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
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
    authenticationResource = new AuthenticationResource(accessTokenBuilderFactory, cookieIssuer);
    dispatcher.addSingletonResource(authenticationResource);

    AccessToken accessToken = mock(AccessToken.class);
    when(accessToken.getExpiration()).thenReturn(new Date(Long.MAX_VALUE));
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
  }

  @Test
  public void shouldAuthCorrectlyWithFormencodedData() throws URISyntaxException {

    MockHttpRequest request = getMockHttpRequestUrlEncoded(AUTH_FORMENCODED_TRILLIAN);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }


  @Test
  public void shouldNotAuthUserWithWrongPassword() throws URISyntaxException {

    MockHttpRequest request = getMockHttpRequest(AUTH_JSON_TRILLIAN_WRONG_PW);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void shouldNotAuthNonexistingUser() throws URISyntaxException {
    MockHttpRequest request = getMockHttpRequest(AUTH_JSON_NOT_EXISTING_USER);

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
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
  }

  @Test
  public void shouldHandleLogoutRedirection() throws URISyntaxException, UnsupportedEncodingException {
    authenticationResource.setLogoutRedirection(() -> of(create("http://example.com/cas/logout")));

    MockHttpRequest request = MockHttpRequest.delete("/" + AuthenticationResource.PATH + "/access_token");

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertThat(response.getContentAsString(), containsString("http://example.com/cas/logout"));
  }

  @Test
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
