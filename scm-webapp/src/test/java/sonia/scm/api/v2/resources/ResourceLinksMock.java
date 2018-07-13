package sonia.scm.api.v2.resources;

import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceLinksMock {
  public static ResourceLinks createMock(URI baseUri) {
    ResourceLinks resourceLinks = mock(ResourceLinks.class);

    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getBaseUri()).thenReturn(baseUri);

    when(resourceLinks.user()).thenReturn(new ResourceLinks.UserLinks(uriInfo));
    when(resourceLinks.userCollection()).thenReturn(new ResourceLinks.UserCollectionLinks(uriInfo));
    when(resourceLinks.group()).thenReturn(new ResourceLinks.GroupLinks(uriInfo));
    when(resourceLinks.groupCollection()).thenReturn(new ResourceLinks.GroupCollectionLinks(uriInfo));
    when(resourceLinks.repository()).thenReturn(new ResourceLinks.RepositoryLinks(uriInfo));
    when(resourceLinks.repositoryCollection()).thenReturn(new ResourceLinks.RepositoryCollectionLinks(uriInfo));
    when(resourceLinks.tagCollection()).thenReturn(new ResourceLinks.TagCollectionLinks(uriInfo));
    when(resourceLinks.branchCollection()).thenReturn(new ResourceLinks.BranchCollectionLinks(uriInfo));
    when(resourceLinks.changesetCollection()).thenReturn(new ResourceLinks.ChangesetCollectionLinks(uriInfo));
    when(resourceLinks.sourceCollection()).thenReturn(new ResourceLinks.SourceCollectionLinks(uriInfo));
    when(resourceLinks.permissionCollection()).thenReturn(new ResourceLinks.PermissionCollectionLinks(uriInfo));
    when(resourceLinks.globalConfig()).thenReturn(new ResourceLinks.GlobalConfigLinks(uriInfo));
    return resourceLinks;
  }
}
