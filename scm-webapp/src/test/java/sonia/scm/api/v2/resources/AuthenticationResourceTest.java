package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AccessToken;
import sonia.scm.security.AccessTokenBuilder;
import sonia.scm.security.AccessTokenBuilderFactory;
import sonia.scm.security.AccessTokenCookieIssuer;
import sonia.scm.user.User;
import sonia.scm.user.UserException;
import sonia.scm.user.UserManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@SubjectAware(
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  @Mock
  private AccessTokenBuilderFactory accessTokenBuilderFactory;

  @Mock
  private AccessTokenBuilder accessTokenBuilder;

  private AccessTokenCookieIssuer cookieIssuer = new AccessTokenCookieIssuer(mock(ScmConfiguration.class));

  private static final String AUTH_JSON_TRILLIAN = "{\n" +
    "\t\"cookie\": true,\n" +
    "\t\"grant_type\": \"password\",\n" +
    "\t\"username\": \"trillian\",\n" +
    "\t\"password\": \"secret\"\n" +
    "}";

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

  @Before
  public void prepareEnvironment() {
    AuthenticationResource authenticationResource = new AuthenticationResource(accessTokenBuilderFactory, cookieIssuer);
    dispatcher.getRegistry().addSingletonResource(authenticationResource);

    AccessToken accessToken = mock(AccessToken.class);
    when(accessToken.getExpiration()).thenReturn(new Date(Long.MAX_VALUE));
    when(accessTokenBuilder.build()).thenReturn(accessToken);

    when(accessTokenBuilderFactory.create()).thenReturn(accessTokenBuilder);

    HttpServletRequest servletRequest = mock(HttpServletRequest.class);
    ResteasyProviderFactory.getContextDataMap().put(HttpServletRequest.class, servletRequest);

    HttpServletResponse servletResponse = mock(HttpServletResponse.class);
    ResteasyProviderFactory.getContextDataMap().put(HttpServletResponse.class, servletResponse);
  }

  @Test
  public void shouldAuthCorrectly() throws URISyntaxException {

    MockHttpRequest request = getMockHttpRequest(AUTH_JSON_TRILLIAN);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  @Test
  public void shouldNotAuthUserWithWrongPassword() throws URISyntaxException {

    MockHttpRequest request = getMockHttpRequest(AUTH_JSON_TRILLIAN_WRONG_PW);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
  }

  @Test
  public void shouldNotAuthNonexistingUser() throws URISyntaxException {
    MockHttpRequest request = getMockHttpRequest(AUTH_JSON_NOT_EXISTING_USER);
    MockHttpResponse response = new MockHttpResponse();

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

  private void shouldReturnBadRequest(String requestBody) throws URISyntaxException {
    MockHttpRequest request = getMockHttpRequest(requestBody);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
  }

  private MockHttpRequest getMockHttpRequest(String jsonPayload) throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.post("/" + AuthenticationResource.PATH + "/access_token");

    request.content(jsonPayload.getBytes());
    request.contentType(MediaType.APPLICATION_JSON_TYPE);
    return request;
  }

}
