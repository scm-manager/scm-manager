package sonia.scm.api.v2.resources;

import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.group.GroupNames;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserTestData;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MeDtoFactoryTest {

  private final URI baseUri = URI.create("https://scm.hitchhiker.com/scm/");

  @Mock
  private UserManager userManager;

  @Mock
  private Subject subject;

  private MeDtoFactory meDtoFactory;

  @BeforeEach
  void setUpContext() {
    ThreadContext.bind(subject);
    ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);
    meDtoFactory = new MeDtoFactory(resourceLinks, userManager);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldCreateMeDtoFromUser() {
    prepareSubject(UserTestData.createTrillian());

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getName()).isEqualTo("trillian");
    assertThat(dto.getDisplayName()).isEqualTo("Tricia McMillan");
    assertThat(dto.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
  }

  @Test
  void shouldCreateMeDtoWithEmptyGroups() {
    prepareSubject(UserTestData.createTrillian());
    MeDto dto = meDtoFactory.create();
    assertThat(dto.getGroups()).isEmpty();
  }

  @Test
  void shouldCreateMeDtoWithGroups() {
    prepareSubject(UserTestData.createTrillian(), "HeartOfGold", "Puzzle42");
    MeDto dto = meDtoFactory.create();
    assertThat(dto.getGroups()).containsOnly("HeartOfGold", "Puzzle42");
  }

  private void prepareSubject(User user, String... groups) {
    PrincipalCollection collection = mock(PrincipalCollection.class);
    when(subject.getPrincipals()).thenReturn(collection);
    when(collection.oneByType(any(Class.class))).then(ic -> {
      Class<?> type = ic.getArgument(0);
      if (type.isAssignableFrom(User.class)) {
        return user;
      } else if (type.isAssignableFrom(GroupNames.class)) {
        return new GroupNames(Lists.newArrayList(groups));
      } else {
        return null;
      }
    });
  }

  @Test
  void shouldAppendSelfLink() {
    prepareSubject(UserTestData.createTrillian());

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("https://scm.hitchhiker.com/scm/v2/me/");
  }

  @Test
  void shouldAppendDeleteLink() {
    prepareSubject(UserTestData.createTrillian());
    when(subject.isPermitted("user:delete:trillian")).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("delete").get().getHref()).isEqualTo("https://scm.hitchhiker.com/scm/v2/users/trillian");
  }

  @Test
  void shouldNotAppendDeleteLink() {
    prepareSubject(UserTestData.createTrillian());

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("delete")).isNotPresent();
  }

  @Test
  void shouldAppendUpdateLink() {
    prepareSubject(UserTestData.createTrillian());
    when(subject.isPermitted("user:modify:trillian")).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("update").get().getHref()).isEqualTo("https://scm.hitchhiker.com/scm/v2/users/trillian");
  }

  @Test
  void shouldNotAppendUpdateLink() {
    prepareSubject(UserTestData.createTrillian());

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldGetPasswordLinkOnlyForDefaultUserType() {
    User user = UserTestData.createTrillian();
    prepareSubject(user);

    when(subject.isPermitted("user:changePassword:trillian")).thenReturn(true);
    when(userManager.isTypeDefault(user)).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("password").get().getHref()).isEqualTo("https://scm.hitchhiker.com/scm/v2/me/password");
  }

  @Test
  void shouldNotGetPasswordLinkWithoutPermision() {
    User user = UserTestData.createTrillian();
    prepareSubject(user);

    when(userManager.isTypeDefault(user)).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("password")).isNotPresent();
  }

  @Test
  void shouldNotGetPasswordLinkForNonDefaultUsers() {
    User user = UserTestData.createTrillian();
    prepareSubject(user);

    when(subject.isPermitted("user:changePassword:trillian")).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("password")).isNotPresent();
  }

  @Test
  void shouldAppendLinks() {
    prepareSubject(UserTestData.createTrillian());

    HalEnricherRegistry registry = new HalEnricherRegistry();
    meDtoFactory.setRegistry(registry);

    registry.register(Me.class, (ctx, appender) -> {
      User user = ctx.oneRequireByType(User.class);
      appender.appendLink("profile", "http://hitchhiker.com/users/" + user.getName());
    });

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("profile").get().getHref()).isEqualTo("http://hitchhiker.com/users/trillian");
  }


}
