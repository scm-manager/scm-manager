package sonia.scm.api.rest.resources;

import org.junit.Test;
import sonia.scm.user.User;

import javax.ws.rs.core.UriInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

public class UserMapperTest {

  @Test
  public void shouldMapLinks() {
    User user = new User();
    user.setName("abc");
    UserDto userDto = UserMapper.INSTANCE.userToUserDto(user, mock(UriInfo.class));
    assertEquals("abc" , userDto.getName());
    assertNotNull("expected map with links", userDto.getLinks());
  }

}
