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

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResourceLinksMock {
  public static ResourceLinks createMock(URI baseUri) {
    ResourceLinks resourceLinks = mock(ResourceLinks.class);

    ScmPathInfo pathInfo = mock(ScmPathInfo.class);
    when(pathInfo.getApiRestUri()).thenReturn(baseUri);

    ResourceLinks.UserLinks userLinks = new ResourceLinks.UserLinks(pathInfo);
    lenient().when(resourceLinks.user()).thenReturn(userLinks);
    lenient().when(resourceLinks.me()).thenReturn(new ResourceLinks.MeLinks(pathInfo,userLinks));
    lenient().when(resourceLinks.userCollection()).thenReturn(new ResourceLinks.UserCollectionLinks(pathInfo));
    lenient().when(resourceLinks.userPermissions()).thenReturn(new ResourceLinks.UserPermissionLinks(pathInfo));
    lenient().when(resourceLinks.autoComplete()).thenReturn(new ResourceLinks.AutoCompleteLinks(pathInfo));
    lenient().when(resourceLinks.group()).thenReturn(new ResourceLinks.GroupLinks(pathInfo));
    lenient().when(resourceLinks.groupCollection()).thenReturn(new ResourceLinks.GroupCollectionLinks(pathInfo));
    lenient().when(resourceLinks.groupPermissions()).thenReturn(new ResourceLinks.GroupPermissionLinks(pathInfo));
    lenient().when(resourceLinks.repository()).thenReturn(new ResourceLinks.RepositoryLinks(pathInfo));
    lenient().when(resourceLinks.incoming()).thenReturn(new ResourceLinks.IncomingLinks(pathInfo));
    lenient().when(resourceLinks.repositoryCollection()).thenReturn(new ResourceLinks.RepositoryCollectionLinks(pathInfo));
    lenient().when(resourceLinks.tag()).thenReturn(new ResourceLinks.TagCollectionLinks(pathInfo));
    lenient().when(resourceLinks.branchCollection()).thenReturn(new ResourceLinks.BranchCollectionLinks(pathInfo));
    lenient().when(resourceLinks.changeset()).thenReturn(new ResourceLinks.ChangesetLinks(pathInfo));
    lenient().when(resourceLinks.fileHistory()).thenReturn(new ResourceLinks.FileHistoryLinks(pathInfo));
    lenient().when(resourceLinks.source()).thenReturn(new ResourceLinks.SourceLinks(pathInfo));
    lenient().when(resourceLinks.repositoryPermission()).thenReturn(new ResourceLinks.RepositoryPermissionLinks(pathInfo));
    lenient().when(resourceLinks.config()).thenReturn(new ResourceLinks.ConfigLinks(pathInfo));
    lenient().when(resourceLinks.branch()).thenReturn(new ResourceLinks.BranchLinks(pathInfo));
    lenient().when(resourceLinks.diff()).thenReturn(new ResourceLinks.DiffLinks(pathInfo));
    lenient().when(resourceLinks.modifications()).thenReturn(new ResourceLinks.ModificationsLinks(pathInfo));
    lenient().when(resourceLinks.repositoryType()).thenReturn(new ResourceLinks.RepositoryTypeLinks(pathInfo));
    lenient().when(resourceLinks.repositoryTypeCollection()).thenReturn(new ResourceLinks.RepositoryTypeCollectionLinks(pathInfo));
    lenient().when(resourceLinks.installedPluginCollection()).thenReturn(new ResourceLinks.InstalledPluginCollectionLinks(pathInfo));
    lenient().when(resourceLinks.availablePluginCollection()).thenReturn(new ResourceLinks.AvailablePluginCollectionLinks(pathInfo));
    lenient().when(resourceLinks.pendingPluginCollection()).thenReturn(new ResourceLinks.PendingPluginCollectionLinks(pathInfo));
    lenient().when(resourceLinks.installedPlugin()).thenReturn(new ResourceLinks.InstalledPluginLinks(pathInfo));
    lenient().when(resourceLinks.availablePlugin()).thenReturn(new ResourceLinks.AvailablePluginLinks(pathInfo));
    lenient().when(resourceLinks.uiPluginCollection()).thenReturn(new ResourceLinks.UIPluginCollectionLinks(pathInfo));
    lenient().when(resourceLinks.uiPlugin()).thenReturn(new ResourceLinks.UIPluginLinks(pathInfo));
    lenient().when(resourceLinks.authentication()).thenReturn(new ResourceLinks.AuthenticationLinks(pathInfo));
    lenient().when(resourceLinks.index()).thenReturn(new ResourceLinks.IndexLinks(pathInfo));
    lenient().when(resourceLinks.permissions()).thenReturn(new ResourceLinks.PermissionsLinks(pathInfo));
    lenient().when(resourceLinks.repositoryVerbs()).thenReturn(new ResourceLinks.RepositoryVerbLinks(pathInfo));
    lenient().when(resourceLinks.repositoryRole()).thenReturn(new ResourceLinks.RepositoryRoleLinks(pathInfo));
    lenient().when(resourceLinks.repositoryRoleCollection()).thenReturn(new ResourceLinks.RepositoryRoleCollectionLinks(pathInfo));
    lenient().when(resourceLinks.namespaceStrategies()).thenReturn(new ResourceLinks.NamespaceStrategiesLinks(pathInfo));
    lenient().when(resourceLinks.annotate()).thenReturn(new ResourceLinks.AnnotateLinks(pathInfo));
    lenient().when(resourceLinks.namespace()).thenReturn(new ResourceLinks.NamespaceLinks(pathInfo));
    lenient().when(resourceLinks.namespaceCollection()).thenReturn(new ResourceLinks.NamespaceCollectionLinks(pathInfo));

    return resourceLinks;
  }
}
