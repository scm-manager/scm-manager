package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;

import java.net.URI;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SubjectAware(configuration = "classpath:sonia/scm/shiro-002.ini")
public class IndexResourceTest {

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  private ScmConfiguration configuration;
  private SCMContextProvider scmContextProvider;
  private IndexResource indexResource;


  @Before
  public void setUpObjectUnderTest() {
    this.configuration = new ScmConfiguration();
    this.scmContextProvider = mock(SCMContextProvider.class);
    IndexDtoGenerator generator = new IndexDtoGenerator(
      ResourceLinksMock.createMock(URI.create("/")),
      scmContextProvider,
      configuration
    );
    this.indexResource = new IndexResource(generator);
  }

  @Test
  public void shouldRenderLoginUrlsForUnauthenticatedRequest() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("login")).matches(Optional::isPresent);
  }

  @Test
  public void shouldRenderLoginInfoUrl() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("loginInfo")).isPresent();
  }

  @Test
  public void shouldNotRenderLoginInfoUrlWhenNoUrlIsConfigured() {
    configuration.setLoginInfoUrl("");

    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("loginInfo")).isNotPresent();
  }

  @Test
  public void shouldRenderSelfLinkForUnauthenticatedRequest() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("self")).matches(Optional::isPresent);
  }

  @Test
  public void shouldRenderUiPluginsLinkForUnauthenticatedRequest() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("uiPlugins")).matches(Optional::isPresent);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldRenderSelfLinkForAuthenticatedRequest() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("self")).matches(Optional::isPresent);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldRenderUiPluginsLinkForAuthenticatedRequest() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("uiPlugins")).matches(Optional::isPresent);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldRenderMeUrlForAuthenticatedRequest() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("me")).matches(Optional::isPresent);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldRenderLogoutUrlForAuthenticatedRequest() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("logout")).matches(Optional::isPresent);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldRenderRepositoriesForAuthenticatedRequest() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("repositories")).matches(Optional::isPresent);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldNotRenderAdminLinksIfNotAuthorized() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("users")).matches(o -> !o.isPresent());
    Assertions.assertThat(index.getLinks().getLinkBy("groups")).matches(o -> !o.isPresent());
    Assertions.assertThat(index.getLinks().getLinkBy("config")).matches(o -> !o.isPresent());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldRenderAutoCompleteLinks() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinksBy("autocomplete"))
      .extracting("name")
      .containsExactlyInAnyOrder("users", "groups");
  }

  @Test
  @SubjectAware(username = "user_without_autocomplete_permission", password = "secret")
  public void userWithoutAutocompletePermissionShouldNotSeeAutoCompleteLinks() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinksBy("autocomplete"))
      .extracting("name")
      .doesNotContainSequence("users", "groups");
  }

  @Test
  @SubjectAware(username = "dent", password = "secret")
  public void shouldRenderAdminLinksIfAuthorized() {
    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getLinks().getLinkBy("users")).matches(Optional::isPresent);
    Assertions.assertThat(index.getLinks().getLinkBy("groups")).matches(Optional::isPresent);
    Assertions.assertThat(index.getLinks().getLinkBy("config")).matches(Optional::isPresent);
  }

  @Test
  public void shouldGenerateVersion() {
    when(scmContextProvider.getVersion()).thenReturn("v1");

    IndexDto index = indexResource.getIndex();

    Assertions.assertThat(index.getVersion()).isEqualTo("v1");
  }
}
