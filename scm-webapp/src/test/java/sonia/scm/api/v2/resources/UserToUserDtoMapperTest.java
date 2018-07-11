package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import sonia.scm.api.rest.resources.UserResource;
import sonia.scm.user.User;

import java.net.URI;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserToUserDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  private UserToUserDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    initMocks(this);
    expectedBaseUri = baseUri.resolve(UserRootResource.USERS_PATH_V2 + "/");
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapLinks_forUpdate() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertEquals("expected self link",   expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("self").get().getHref());
    assertEquals("expected update link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldMapLinks_forDelete() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:delete:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertEquals("expected self link",   expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("self").get().getHref());
    assertEquals("expected delete link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("delete").get().getHref());
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

    UserDto userDto = mapper.map(user);

    assertEquals("expected self link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("self").get().getHref());
    assertFalse("expected no delete link", userDto.getLinks().getLinkBy("delete").isPresent());
    assertFalse("expected no update link", userDto.getLinks().getLinkBy("update").isPresent());
  }

  @Test
  public void shouldMapFields() {
    User user = createDefaultUser();

    UserDto userDto = mapper.map(user);

    assertEquals("abc", userDto.getName());
  }

  @Test
  public void shouldRemovePassword() {
    User user = createDefaultUser();
    user.setPassword("password");

    UserDto userDto = mapper.map(user);

    assertEquals(UserResource.DUMMY_PASSWORT, userDto.getPassword());
  }

  @Test
  public void shouldMapTimes() {
    User user = createDefaultUser();
    Instant expectedCreationDate = Instant.ofEpochSecond(6666666);
    Instant expectedModificationDate = expectedCreationDate.plusSeconds(1);
    user.setCreationDate(expectedCreationDate.toEpochMilli());
    user.setLastModified(expectedModificationDate.toEpochMilli());

    UserDto userDto = mapper.map(user);

    assertEquals(expectedCreationDate, userDto.getCreationDate());
    assertEquals(expectedModificationDate, userDto.getLastModified());
  }
}
