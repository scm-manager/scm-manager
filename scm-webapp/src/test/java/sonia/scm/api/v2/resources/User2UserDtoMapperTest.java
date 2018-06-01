package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import sonia.scm.api.rest.resources.UserResource;
import sonia.scm.user.User;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class User2UserDtoMapperTest {

  private final User2UserDtoMapper mapper = Mappers.getMapper(User2UserDtoMapper.class);
  private final UriInfo uriInfo = mock(UriInfo.class);
  private final Subject subject = mock(Subject.class);
  private ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI baseUri;

  @Before
  public void init() throws URISyntaxException {
    baseUri = new URI("http://example.com/base/");
    when(uriInfo.getBaseUri()).thenReturn(baseUri);
    subjectThreadState.bind();
  }

  @Test
  public void shouldMapLinks_forAdmin() {
    User user = new User();
    user.setName("abc");
    when(subject.hasRole("admin")).thenReturn(true);

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals("expected map with self baseUri", baseUri.resolve("v2/users/abc"), userDto.getLinks().get("self").getHref());
    assertEquals("expected map with delete baseUri", baseUri.resolve("v2/users/abc"), userDto.getLinks().get("delete").getHref());
    assertEquals("expected map with update baseUri", baseUri.resolve("v2/users/abc"), userDto.getLinks().get("update").getHref());
    assertEquals("expected map with create baseUri", baseUri.resolve("v2/users"), userDto.getLinks().get("create").getHref());
  }

  @Test
  public void shouldMapLinks_forNormalUser() {
    User user = new User();
    user.setName("abc");
    when(subject.hasRole("user")).thenReturn(true);

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals("expected map with self baseUri", baseUri.resolve("v2/users/abc"), userDto.getLinks().get("self").getHref());
    assertNull("expected map without delete baseUri", userDto.getLinks().get("delete"));
    assertNull("expected map without update baseUri", userDto.getLinks().get("update"));
    assertNull("expected map without create baseUri", userDto.getLinks().get("create"));
  }

  @Test
  public void shouldMapFields() {
    User user = new User();
    user.setName("abc");

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals("abc", userDto.getName());
  }

  @Test
  public void shouldRemovePassword() {
    User user = new User();
    user.setPassword("password");
    user.setName("abc");

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals(UserResource.DUMMY_PASSWORT, userDto.getPassword());
  }
}
