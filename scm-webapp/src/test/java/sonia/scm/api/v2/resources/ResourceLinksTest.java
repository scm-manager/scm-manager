package sonia.scm.api.v2.resources;

import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.api.v2.resources.ResourceLinks.group;
import static sonia.scm.api.v2.resources.ResourceLinks.user;
import static sonia.scm.api.v2.resources.ResourceLinks.userCollection;

public class ResourceLinksTest {

  private static final String BASE_URL = "http://example.com/";

  private UriInfo uriInfo = mock(UriInfo.class);

  @Test
  public void shouldCreateCorrectUserSelfUrl() {
    String url = user(uriInfo).self("ich");
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2 + "ich", url);
  }

  @Test
  public void shouldCreateCorrectUserDeleteUrl() {
    String url = user(uriInfo).delete("ich");
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2 + "ich", url);
  }

  @Test
  public void shouldCreateCorrectUserUpdateUrl() {
    String url = user(uriInfo).update("ich");
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2 + "ich", url);
  }

  @Test
  public void shouldCreateCorrectUserCreateUrl() {
    String url = userCollection(uriInfo).create();
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2, url);
  }

  @Test
  public void shouldCreateCorrectUserCollectionUrl() {
    String url = userCollection(uriInfo).self();
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2, url);
  }

  @Test
  public void shouldCreateCorrectGroupSelfUrl() {
    String url = group(uriInfo).self("nobodies");
    assertEquals(BASE_URL + GroupV2Resource.GROUPS_PATH_V2 + "nobodies", url);
  }

  @Test
  public void shouldCreateCorrectGroupDeleteUrl() {
    String url = group(uriInfo).delete("nobodies");
    assertEquals(BASE_URL + GroupV2Resource.GROUPS_PATH_V2 + "nobodies", url);
  }

  @Test
  public void shouldCreateCorrectGroupUpdateUrl() {
    String url = group(uriInfo).update("nobodies");
    assertEquals(BASE_URL + GroupV2Resource.GROUPS_PATH_V2 + "nobodies", url);
  }

//  @Test
//  public void shouldCreateCorrectGroupCreateUrl() {
//    String url = ResourceLinks.groupCollection(uriInfo).create();
//    assertEquals(BASE_URL + GroupV2Resource.GROUPS_PATH_V2, url);
//  }
//
//  @Test
//  public void shouldCreateCorrectGroupCollectionUrl() {
//    String url = ResourceLinks.groupCollection(uriInfo).self();
//    assertEquals(BASE_URL + GroupV2Resource.GROUPS_PATH_V2, url);
//  }

  @Before
  public void initUriInfo() {
    when(uriInfo.getBaseUri()).thenReturn(URI.create(BASE_URL));
  }
}
