/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.net.URI;

import static com.google.inject.util.Providers.of;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ResourceLinksTest {

  private static final String BASE_URL = "http://example.com/";

  @Mock
  private ScmPathInfoStore scmPathInfoStore;
  @Mock
  private ScmPathInfo uriInfo;

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
  public void shouldCreateCorrectUserApiKeysUrl() {
    String url = resourceLinks.user().apiKeys("ich");
    assertEquals(BASE_URL + UserRootResource.USERS_PATH_V2 + "ich/api_keys", url);
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
  public void shouldCreateCorrectRepositorySelfUrl() {
    String url = resourceLinks.repository().self("space", "repo");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo", url);
  }

  @Test
  public void shouldCreateCorrectRepositoryDeleteUrl() {
    String url = resourceLinks.repository().delete("space", "repo");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo", url);
  }

  @Test
  public void shouldCreateCorrectRepositoryUpdateUrl() {
    String url = resourceLinks.repository().update("space", "repo");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo", url);
  }

  @Test
  public void shouldCreateCorrectTagCollectionUrl() {
    String url = resourceLinks.tag().all("space", "repo");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/tags/", url);
  }

  @Test
  public void shouldCreateCorrectBranchCollectionUrl() {
    String url = resourceLinks.branchCollection().self("space", "repo");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/branches/", url);
  }

  @Test
  public void shouldCreateCorrectBranchUrl() {
    String url = resourceLinks.branch().self("space", "name", "master");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/name/branches/master", url);
  }

  @Test
  public void shouldCreateCorrectBranchHiostoryUrl() {
    String url = resourceLinks.branch().history("space", "name", "master");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/name/branches/master/changesets/", url);
  }

  @Test
  public void shouldCreateCorrectBranchChangesetUrl() {
    String url = resourceLinks.changeset().changeset("space", "name", "revision");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/name/changesets/revision", url);
  }

  @Test
  public void shouldCreateCorrectBranchSourceUrl() {
    String url = resourceLinks.source().selfWithoutRevision("space", "name");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/name/sources/", url);
  }

  @Test
  public void shouldCreateCorrectChangesetCollectionUrl() {
    String url = resourceLinks.changeset().all("space", "repo");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/changesets/", url);
  }

  @Test
  public void shouldCreateCorrectSourceCollectionUrl() {
    String url = resourceLinks.source().selfWithoutRevision("space", "repo");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/sources/", url);
  }

  @Test
  public void shouldCreateCorrectSourceUrlWithFilename() {
    String url = resourceLinks.source().sourceWithPath("foo", "bar", "rev", "file");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "foo/bar/sources/rev/file", url);
  }

  @Test
  public void shouldCreateCorrectPermissionCollectionUrl() {
    String url = resourceLinks.source().selfWithoutRevision("space", "repo");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/repo/sources/", url);
  }

  @Test
  public void shouldCreateCorrectConfigSelfUrl() {
    String url = resourceLinks.config().self();
    assertEquals(BASE_URL + ConfigResource.CONFIG_PATH_V2, url);
  }

  @Test
  public void shouldCreateCorrectConfigUpdateUrl() {
    String url = resourceLinks.config().update();
    assertEquals(BASE_URL + ConfigResource.CONFIG_PATH_V2, url);
  }

  @Test
  public void shouldHandleSpacesInPaths() {
    String url = resourceLinks.source().content("space", "name", "rev", "name with spaces");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/name/content/rev/name%20with%20spaces", url);
  }

  @Test
  public void shouldHandleBackslashInPaths() {
    String url = resourceLinks.source().content("space", "name", "rev", "name_with_\\");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/name/content/rev/name_with_%5C", url);
  }

  @Test
  public void shouldHandleNewLineInPaths() {
    String url = resourceLinks.source().content("space", "name", "rev", "name_with\nnew_line");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/name/content/rev/name_with%0Anew_line", url);
  }

  @Test
  public void shouldKeepSlashesInInPaths() {
    String url = resourceLinks.source().content("space", "name", "rev", "some/dir/somewhere");
    assertEquals(BASE_URL + RepositoryRootResource.REPOSITORIES_PATH_V2 + "space/name/content/rev/some/dir/somewhere", url);
  }

  @Before
  public void initUriInfo() {
    when(scmPathInfoStore.get()).thenReturn(uriInfo);
    when(uriInfo.getApiRestUri()).thenReturn(URI.create(BASE_URL));
    resourceLinks = new ResourceLinks(of(scmPathInfoStore));
  }
}
