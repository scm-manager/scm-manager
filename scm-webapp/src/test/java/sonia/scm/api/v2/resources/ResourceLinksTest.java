package sonia.scm.api.v2.resources;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ResourceLinksTest {

  private static final String BASE_URL = "http://example.com/";

  @Mock
  private UriInfoStore uriInfoStore;
  @Mock
  private UriInfo uriInfo;

  @InjectMocks
  private ResourceLinks resourceLinks;

  @Test
  public void shouldCreateCorrectUserSelfUrl() {
    String url = resourceLinks.user().self("ich");
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2 + "ich", url);
  }

  @Test
  public void shouldCreateCorrectUserDeleteUrl() {
    String url = resourceLinks.user().delete("ich");
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2 + "ich", url);
  }

  @Test
  public void shouldCreateCorrectUserUpdateUrl() {
    String url = resourceLinks.user().update("ich");
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2 + "ich", url);
  }

  @Test
  public void shouldCreateCorrectUserCreateUrl() {
    String url = resourceLinks.userCollection().create();
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2, url);
  }

  @Test
  public void shouldCreateCorrectUserCollectionUrl() {
    String url = resourceLinks.userCollection().self();
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2, url);
  }

  @Test
  public void shouldCreateCorrectGroupSelfUrl() {
    String url = resourceLinks.group().self("nobodies");
    assertEquals(BASE_URL + GroupRootResource.GROUPS_PATH_V2 + "nobodies", url);
  }

  @Test
  public void shouldCreateCorrectGroupDeleteUrl() {
    String url = resourceLinks.group().delete("nobodies");
    assertEquals(BASE_URL + GroupRootResource.GROUPS_PATH_V2 + "nobodies", url);
  }

  @Test
  public void shouldCreateCorrectGroupUpdateUrl() {
    String url = resourceLinks.group().update("nobodies");
    assertEquals(BASE_URL + GroupRootResource.GROUPS_PATH_V2 + "nobodies", url);
  }

  @Test
  public void shouldCreateCorrectGroupCreateUrl() {
    String url = resourceLinks.groupCollection().create();
    assertEquals(BASE_URL + GroupRootResource.GROUPS_PATH_V2, url);
  }

  @Test
  public void shouldCreateCorrectGroupCollectionUrl() {
    String url = resourceLinks.groupCollection().self();
    assertEquals(BASE_URL + GroupRootResource.GROUPS_PATH_V2, url);
  }

  @Test
  public void shouldCreateCorrectGlobalConfigSelfUrl() {
    String url = resourceLinks.globalConfig().self();
    assertEquals(BASE_URL + GlobalConfigResource.GLOBAL_CONFIG_PATH_V2, url);
  }

  @Test
  public void shouldCreateCorrectGlobalConfigUpdateUrl() {
    String url = resourceLinks.globalConfig().update();
    assertEquals(BASE_URL + GlobalConfigResource.GLOBAL_CONFIG_PATH_V2, url);
  }

  @Before
  public void initUriInfo() {
    initMocks(this);
    when(uriInfoStore.get()).thenReturn(uriInfo);
    when(uriInfo.getBaseUri()).thenReturn(URI.create(BASE_URL));
  }
}
