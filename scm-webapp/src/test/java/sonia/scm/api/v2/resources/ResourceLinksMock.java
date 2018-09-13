package sonia.scm.api.v2.resources;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceLinksMock {
  public static ResourceLinks createMock(URI baseUri) {
    ResourceLinks resourceLinks = mock(ResourceLinks.class);

    ScmPathInfo uriInfo = mock(ScmPathInfo.class);
    when(uriInfo.getApiRestUri()).thenReturn(baseUri);

    when(resourceLinks.user()).thenReturn(new ResourceLinks.UserLinks(uriInfo));
    when(resourceLinks.userCollection()).thenReturn(new ResourceLinks.UserCollectionLinks(uriInfo));
    when(resourceLinks.group()).thenReturn(new ResourceLinks.GroupLinks(uriInfo));
    when(resourceLinks.groupCollection()).thenReturn(new ResourceLinks.GroupCollectionLinks(uriInfo));
    when(resourceLinks.repository()).thenReturn(new ResourceLinks.RepositoryLinks(uriInfo));
    when(resourceLinks.repositoryCollection()).thenReturn(new ResourceLinks.RepositoryCollectionLinks(uriInfo));
    when(resourceLinks.tag()).thenReturn(new ResourceLinks.TagCollectionLinks(uriInfo));
    when(resourceLinks.branchCollection()).thenReturn(new ResourceLinks.BranchCollectionLinks(uriInfo));
    when(resourceLinks.changeset()).thenReturn(new ResourceLinks.ChangesetLinks(uriInfo));
    when(resourceLinks.fileHistory()).thenReturn(new ResourceLinks.FileHistoryLinks(uriInfo));
    when(resourceLinks.source()).thenReturn(new ResourceLinks.SourceLinks(uriInfo));
    when(resourceLinks.permission()).thenReturn(new ResourceLinks.PermissionLinks(uriInfo));
    when(resourceLinks.config()).thenReturn(new ResourceLinks.ConfigLinks(uriInfo));
    when(resourceLinks.branch()).thenReturn(new ResourceLinks.BranchLinks(uriInfo));
    when(resourceLinks.diff()).thenReturn(new ResourceLinks.DiffLinks(uriInfo));
    when(resourceLinks.repositoryType()).thenReturn(new ResourceLinks.RepositoryTypeLinks(uriInfo));
    when(resourceLinks.repositoryTypeCollection()).thenReturn(new ResourceLinks.RepositoryTypeCollectionLinks(uriInfo));
    when(resourceLinks.uiPluginCollection()).thenReturn(new ResourceLinks.UIPluginCollectionLinks(uriInfo));
    when(resourceLinks.uiPlugin()).thenReturn(new ResourceLinks.UIPluginLinks(uriInfo));

    return resourceLinks;
  }
}
