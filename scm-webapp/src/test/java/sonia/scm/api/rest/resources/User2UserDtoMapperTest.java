package sonia.scm.api.rest.resources;

import org.junit.Before;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import sonia.scm.user.User;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class User2UserDtoMapperTest {

  private final User2UserDtoMapper mapper = Mappers.getMapper(User2UserDtoMapper.class);
  private final UriInfo uriInfo = mock(UriInfo.class);

  private URI baseUri;

  @Before
  public void init() throws URISyntaxException {
    baseUri = new URI("http://example.com/base/");
    when(uriInfo.getBaseUri()).thenReturn(baseUri);
  }

  @Test
  public void shouldMapLinks() {
    User user = new User();
    user.setName("abc");

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals("expected map with self baseUri", baseUri.resolve("usersnew/abc"), userDto.getLinks().get("self").getHref());
    assertEquals("expected map with delete baseUri", baseUri.resolve("usersnew/abc"), userDto.getLinks().get("delete").getHref());
    assertEquals("expected map with update baseUri", baseUri.resolve("usersnew/abc"), userDto.getLinks().get("update").getHref());
    assertEquals("expected map with create baseUri", baseUri.resolve("usersnew"), userDto.getLinks().get("create").getHref());
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
