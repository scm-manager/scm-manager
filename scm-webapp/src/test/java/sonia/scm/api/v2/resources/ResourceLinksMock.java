/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.api.v2.resources;

import java.net.URI;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceLinksMock {
  public static ResourceLinks createMock(URI baseUri) {
    ResourceLinks resourceLinks = mock(ResourceLinks.class);

    ScmPathInfo uriInfo = mock(ScmPathInfo.class);
    when(uriInfo.getApiRestUri()).thenReturn(baseUri);

    ResourceLinks.UserLinks userLinks = new ResourceLinks.UserLinks(uriInfo);
    when(resourceLinks.user()).thenReturn(userLinks);
    when(resourceLinks.me()).thenReturn(new ResourceLinks.MeLinks(uriInfo,userLinks));
    when(resourceLinks.userCollection()).thenReturn(new ResourceLinks.UserCollectionLinks(uriInfo));
    when(resourceLinks.userPermissions()).thenReturn(new ResourceLinks.UserPermissionLinks(uriInfo));
    when(resourceLinks.autoComplete()).thenReturn(new ResourceLinks.AutoCompleteLinks(uriInfo));
    when(resourceLinks.group()).thenReturn(new ResourceLinks.GroupLinks(uriInfo));
    when(resourceLinks.groupCollection()).thenReturn(new ResourceLinks.GroupCollectionLinks(uriInfo));
    when(resourceLinks.groupPermissions()).thenReturn(new ResourceLinks.GroupPermissionLinks(uriInfo));
    when(resourceLinks.repository()).thenReturn(new ResourceLinks.RepositoryLinks(uriInfo));
    when(resourceLinks.incoming()).thenReturn(new ResourceLinks.IncomingLinks(uriInfo));
    when(resourceLinks.repositoryCollection()).thenReturn(new ResourceLinks.RepositoryCollectionLinks(uriInfo));
    when(resourceLinks.tag()).thenReturn(new ResourceLinks.TagCollectionLinks(uriInfo));
    when(resourceLinks.branchCollection()).thenReturn(new ResourceLinks.BranchCollectionLinks(uriInfo));
    when(resourceLinks.changeset()).thenReturn(new ResourceLinks.ChangesetLinks(uriInfo));
    when(resourceLinks.fileHistory()).thenReturn(new ResourceLinks.FileHistoryLinks(uriInfo));
    when(resourceLinks.source()).thenReturn(new ResourceLinks.SourceLinks(uriInfo));
    when(resourceLinks.repositoryPermission()).thenReturn(new ResourceLinks.RepositoryPermissionLinks(uriInfo));
    when(resourceLinks.config()).thenReturn(new ResourceLinks.ConfigLinks(uriInfo));
    when(resourceLinks.branch()).thenReturn(new ResourceLinks.BranchLinks(uriInfo));
    when(resourceLinks.diff()).thenReturn(new ResourceLinks.DiffLinks(uriInfo));
    when(resourceLinks.modifications()).thenReturn(new ResourceLinks.ModificationsLinks(uriInfo));
    when(resourceLinks.repositoryType()).thenReturn(new ResourceLinks.RepositoryTypeLinks(uriInfo));
    when(resourceLinks.repositoryTypeCollection()).thenReturn(new ResourceLinks.RepositoryTypeCollectionLinks(uriInfo));
    when(resourceLinks.installedPluginCollection()).thenReturn(new ResourceLinks.InstalledPluginCollectionLinks(uriInfo));
    when(resourceLinks.availablePluginCollection()).thenReturn(new ResourceLinks.AvailablePluginCollectionLinks(uriInfo));
    when(resourceLinks.pendingPluginCollection()).thenReturn(new ResourceLinks.PendingPluginCollectionLinks(uriInfo));
    when(resourceLinks.installedPlugin()).thenReturn(new ResourceLinks.InstalledPluginLinks(uriInfo));
    when(resourceLinks.availablePlugin()).thenReturn(new ResourceLinks.AvailablePluginLinks(uriInfo));
    when(resourceLinks.uiPluginCollection()).thenReturn(new ResourceLinks.UIPluginCollectionLinks(uriInfo));
    when(resourceLinks.uiPlugin()).thenReturn(new ResourceLinks.UIPluginLinks(uriInfo));
    when(resourceLinks.authentication()).thenReturn(new ResourceLinks.AuthenticationLinks(uriInfo));
    when(resourceLinks.index()).thenReturn(new ResourceLinks.IndexLinks(uriInfo));
    when(resourceLinks.permissions()).thenReturn(new ResourceLinks.PermissionsLinks(uriInfo));
    when(resourceLinks.repositoryVerbs()).thenReturn(new ResourceLinks.RepositoryVerbLinks(uriInfo));
    when(resourceLinks.repositoryRole()).thenReturn(new ResourceLinks.RepositoryRoleLinks(uriInfo));
    when(resourceLinks.repositoryRoleCollection()).thenReturn(new ResourceLinks.RepositoryRoleCollectionLinks(uriInfo));
    when(resourceLinks.namespaceStrategies()).thenReturn(new ResourceLinks.NamespaceStrategiesLinks(uriInfo));

    return resourceLinks;
  }
}
