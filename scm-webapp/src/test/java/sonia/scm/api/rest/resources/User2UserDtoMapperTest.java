package sonia.scm.api.rest.resources;

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

  @Test
  public void shouldMapLinks() throws URISyntaxException {
    URI link = new URI("link");
    when(uriInfo.getAbsolutePath()).thenReturn(link);

    User user = new User();

    UserDto userDto = mapper.userToUserDto(user, uriInfo);
    assertEquals("expected map with self links", link, userDto.getLinks().get("self").getHref());
  }

  @Test
  public void shouldMapFields() {
    User user = new User();
    user.setName("abc");
    UserDto userDto = mapper.userToUserDto(user, uriInfo);
    assertEquals("abc" , userDto.getName());
  }

  @Test
  public void shouldRemovePassword() {
    User user = new User();
    user.setPassword("password");
    UserDto userDto = mapper.userToUserDto(user, uriInfo);
    assertEquals(UserResource.DUMMY_PASSWORT , userDto.getPassword());
  }
}
