package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.repository.Permission;
import sonia.scm.repository.PermissionType;
import sonia.scm.repository.Repository;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.Silent.class)
@SubjectAware(
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class PermissionToPermissionDtoMapperTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private final URI baseUri = URI.create("http://example.com/base/");

  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @InjectMocks
  PermissionToPermissionDtoMapperImpl mapper;

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldMapGroupPermissionCorrectly() {
    Repository repository = getDummyRepository();
    Permission permission = new Permission("42", PermissionType.OWNER, true);

    PermissionDto permissionDto = mapper.map(permission, repository);

    assertThat(permissionDto.getLinks().getLinkBy("self").isPresent()).isTrue();
    assertThat(permissionDto.getLinks().getLinkBy("self").get().getHref()).contains("@42");
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldMapNonGroupPermissionCorrectly() {
    Repository repository = getDummyRepository();
    Permission permission = new Permission("42", PermissionType.OWNER, false);

    PermissionDto permissionDto = mapper.map(permission, repository);

    assertThat(permissionDto.getLinks().getLinkBy("self").isPresent()).isTrue();
    assertThat(permissionDto.getLinks().getLinkBy("self").get().getHref()).contains("42");
    assertThat(permissionDto.getLinks().getLinkBy("self").get().getHref()).doesNotContain("@");
  }

  private Repository getDummyRepository() {
    return new Repository("repo", "git", "foo", "bar");
  }
}
