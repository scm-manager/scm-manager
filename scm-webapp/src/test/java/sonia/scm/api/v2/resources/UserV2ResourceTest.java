package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import javax.servlet.http.HttpServletResponse;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class UserV2ResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  @Before
  public void prepareEnvironment() {
    UserManager userManager = mock(UserManager.class);
    when(userManager.getAll()).thenReturn(Collections.singletonList(createDummyUser()));

    UserDto2UserMapperImpl dtoToUserMapper = new UserDto2UserMapperImpl();
    User2UserDtoMapperImpl userToDtoMapper = new User2UserDtoMapperImpl();
    UserCollectionResource userCollectionResource = new UserCollectionResource(userManager, dtoToUserMapper, userToDtoMapper);
    UserSubResource userSubResource = new UserSubResource(dtoToUserMapper, userToDtoMapper, userManager);
    UserV2Resource userV2Resource = new UserV2Resource(userCollectionResource, userSubResource);

    dispatcher.getRegistry().addSingletonResource(userV2Resource);
  }

  @Test
  public void shouldCreateFullResponseForAdmin() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserV2Resource.USERS_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"password\":\"__dummypassword__\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/Neo\"}"));
    assertTrue(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/Neo\"}"));
  }

  @Test
  @SubjectAware(username = "unpriv")
  public void shouldCreateLimitedResponseForAdmin() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + UserV2Resource.USERS_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"Neo\""));
    assertTrue(response.getContentAsString().contains("\"password\":\"__dummypassword__\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/users/Neo\"}"));
    assertFalse(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/users/Neo\"}"));
  }

  private User createDummyUser() {
    User user = new User();
    user.setName("Neo");
    user.setPassword("redpill");
    user.setCreationDate(System.currentTimeMillis());
    return user;
  }
}
