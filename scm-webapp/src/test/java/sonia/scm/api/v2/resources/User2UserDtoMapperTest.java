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
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class User2UserDtoMapperTest {

  private final User2UserDtoMapper mapper = Mappers.getMapper(User2UserDtoMapper.class);
  private final UriInfo uriInfo = mock(UriInfo.class);
  private final Subject subject = mock(Subject.class);
  private ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() throws URISyntaxException {
    URI baseUri = new URI("http://example.com/base/");
    expectedBaseUri = baseUri.resolve(UserV2Resource.USERS_PATH_V2 + "/");
    when(uriInfo.getBaseUri()).thenReturn(baseUri);
    subjectThreadState.bind();
  }

  @Test
  public void shouldMapLinks_forAdmin() {
    User user = createDefaultUser();
    when(subject.hasRole("admin")).thenReturn(true);

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals("expected self link",   expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("self").get().getHref());
    assertEquals("expected delete link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("delete").get().getHref());
    assertEquals("expected update link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("update").get().getHref());
    assertEquals("expected create link", expectedBaseUri.toString(), userDto.getLinks().getLinkBy("create").get().getHref());
  }

  private User createDefaultUser() {
    User user = new User();
    user.setName("abc");
    user.setCreationDate(1L);
    return user;
  }

  @Test
  public void shouldMapLinks_forNormalUser() {
    User user = createDefaultUser();
    when(subject.hasRole("user")).thenReturn(true);

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals("expected self link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("self").get().getHref());
    assertFalse("expected no delete link", userDto.getLinks().getLinkBy("delete").isPresent());
    assertFalse("expected no update link", userDto.getLinks().getLinkBy("update").isPresent());
    assertFalse("expected no create link", userDto.getLinks().getLinkBy("create").isPresent());
  }

  @Test
  public void shouldMapFields() {
    User user = createDefaultUser();

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals("abc", userDto.getName());
  }

  @Test
  public void shouldRemovePassword() {
    User user = createDefaultUser();
    user.setPassword("password");

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals(UserResource.DUMMY_PASSWORT, userDto.getPassword());
  }

  @Test
  public void shouldMapTimes() {
    User user = createDefaultUser();
    Instant expectedCreationDate = Instant.ofEpochSecond(6666666);
    Instant expectedModificationDate = expectedCreationDate.plusSeconds(1);
    user.setCreationDate(expectedCreationDate.toEpochMilli());
    user.setLastModified(expectedModificationDate.toEpochMilli());

    UserDto userDto = mapper.userToUserDto(user, uriInfo);

    assertEquals(expectedCreationDate, userDto.getCreationDate());
    assertEquals(expectedModificationDate, userDto.getLastModified().get());
  }
}
