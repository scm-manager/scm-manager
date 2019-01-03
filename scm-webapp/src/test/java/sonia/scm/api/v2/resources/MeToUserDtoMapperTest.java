package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserTestData;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class MeToUserDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private UserManager userManager;

  @InjectMocks
  private MeToUserDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;
  private URI expectedUserBaseUri;

  @Before
  public void init() {
    initMocks(this);
    when(userManager.getDefaultType()).thenReturn("xml");
    expectedBaseUri = baseUri.resolve(MeResource.ME_PATH_V2 + "/");
    expectedUserBaseUri = baseUri.resolve(UserRootResource.USERS_PATH_V2 + "/");
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapTheUpdateLink() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);
    assertEquals("expected update link", expectedUserBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("update").get().getHref());

    when(subject.isPermitted("user:modify:abc")).thenReturn(false);
    userDto = mapper.map(user);
    assertFalse("expected no update link", userDto.getLinks().getLinkBy("update").isPresent());
  }

  @Test
  public void shouldMapTheSelfLink() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);
    assertEquals("expected self link", expectedBaseUri.toString(), userDto.getLinks().getLinkBy("self").get().getHref());

  }

  @Test
  public void shouldMapTheDeleteLink() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:delete:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);
    assertEquals("expected update link", expectedUserBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("delete").get().getHref());

    when(subject.isPermitted("user:delete:abc")).thenReturn(false);
    userDto = mapper.map(user);
    assertFalse("expected no delete link", userDto.getLinks().getLinkBy("delete").isPresent());
  }

  @Test
  public void shouldGetPasswordLinkOnlyForDefaultUserType() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);
    when(userManager.isTypeDefault(eq(user))).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertEquals("expected password link with modify permission", expectedBaseUri.resolve("password").toString(), userDto.getLinks().getLinkBy("password").get().getHref());

    when(subject.isPermitted("user:modify:abc")).thenReturn(false);
    userDto = mapper.map(user);
    assertEquals("expected password link on mission modify permission", expectedBaseUri.resolve("password").toString(), userDto.getLinks().getLinkBy("password").get().getHref());

    when(userManager.isTypeDefault(eq(user))).thenReturn(false);

    userDto = mapper.map(user);

    assertFalse("expected no password link", userDto.getLinks().getLinkBy("password").isPresent());
  }


  @Test
  public void shouldGetEmptyPasswordProperty() {
    User user = createDefaultUser();
    user.setPassword("myHighSecurePassword");
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertThat(userDto.getPassword()).as("hide password for the me resource").isBlank();
  }

  @Test
  public void shouldAppendLinks() {
    LinkEnricherRegistry registry = new LinkEnricherRegistry();
    registry.register(Me.class, (ctx, appender) -> {
      User user = ctx.oneRequireByType(User.class);
      appender.appendOne("profile", "http://hitchhiker.com/users/" + user.getName());
    });
    mapper.setRegistry(registry);

    User trillian = UserTestData.createTrillian();
    UserDto dto = mapper.map(trillian);

    assertEquals("http://hitchhiker.com/users/trillian", dto.getLinks().getLinkBy("profile").get().getHref());
  }

  private User createDefaultUser() {
    User user = new User();
    user.setName("abc");
    user.setCreationDate(1L);
    return user;
  }


}
