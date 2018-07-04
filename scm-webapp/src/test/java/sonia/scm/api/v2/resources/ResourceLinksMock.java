package sonia.scm.api.v2.resources;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceLinksMock {
  public static void initMock(ResourceLinks resourceLinks, URI baseUri) {

    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getBaseUri()).thenReturn(baseUri);

    when(resourceLinks.user()).thenReturn(new ResourceLinks.UserLinks(uriInfo));
    when(resourceLinks.userCollection()).thenReturn(new ResourceLinks.UserCollectionLinks(uriInfo));
    when(resourceLinks.group()).thenReturn(new ResourceLinks.GroupLinks(uriInfo));
    when(resourceLinks.groupCollection()).thenReturn(new ResourceLinks.GroupCollectionLinks(uriInfo));
    when(resourceLinks.repository()).thenReturn(new ResourceLinks.RepositoryLinks(uriInfo));
    when(resourceLinks.tagCollection()).thenReturn(new ResourceLinks.TagCollectionLinks(uriInfo));
    when(resourceLinks.branchCollection()).thenReturn(new ResourceLinks.BranchCollectionLinks(uriInfo));
  }
}
