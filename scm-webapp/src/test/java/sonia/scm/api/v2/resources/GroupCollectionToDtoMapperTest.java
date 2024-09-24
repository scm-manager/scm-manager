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

import com.google.inject.util.Providers;
import de.otto.edison.hal.HalRepresentation;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.PageResult;
import sonia.scm.group.Group;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sonia.scm.PageResult.createPage;

public class GroupCollectionToDtoMapperTest {

  private final ScmPathInfo uriInfo = mock(ScmPathInfo.class);
  private final ScmPathInfoStore scmPathInfoStore = new ScmPathInfoStore();
  private final ResourceLinks resourceLinks = new ResourceLinks(Providers.of(scmPathInfoStore));
  private final GroupToGroupDtoMapper groupToDtoMapper = mock(GroupToGroupDtoMapper.class);
  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private final GroupCollectionToDtoMapper mapper = new GroupCollectionToDtoMapper(groupToDtoMapper, resourceLinks);

  private URI expectedBaseUri;

  @Before
  public void init() throws URISyntaxException {
    scmPathInfoStore.set(uriInfo);
    URI baseUri = new URI("http://example.com/base/");
    expectedBaseUri = baseUri.resolve(GroupRootResource.GROUPS_PATH_V2 + "/");
    when(uriInfo.getApiRestUri()).thenReturn(baseUri);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldSetPageNumber() {
    PageResult<Group> pageResult = mockPageResult("nobodies");
    CollectionDto collectionDto = mapper.map(1, 1, pageResult);
    assertEquals(1, collectionDto.getPage());
  }

  @Test
  public void shouldHaveSelfLink() {
    PageResult<Group> pageResult = mockPageResult("nobodies");
    CollectionDto collectionDto = mapper.map(1, 1, pageResult);
    assertTrue(collectionDto.getLinks().getLinkBy("self").get().getHref().startsWith(expectedBaseUri.toString()));
  }

  @Test
  public void shouldCreateNextPageLink_whenHasMore() {
    PageResult<Group> pageResult = createPage(createGroups("nobodies", "bosses"), 0, 1);
    CollectionDto collectionDto = mapper.map(0, 1, pageResult);
    assertTrue(collectionDto.getLinks().getLinkBy("next").get().getHref().contains("page=1"));
  }

  @Test
  public void shouldNotCreateNextPageLink_whenNoMore() {
    PageResult<Group> pageResult = mockPageResult("nobodies");
    CollectionDto collectionDto = mapper.map(1, 1, pageResult);
    assertFalse(collectionDto.getLinks().stream().anyMatch(link -> link.getHref().contains("page=2")));
  }

  @Test
  public void shouldHaveCreateLink_whenHasPermission() {
    PageResult<Group> pageResult = mockPageResult("nobodies");
    when(subject.isPermitted("group:create")).thenReturn(true);

    CollectionDto collectionDto = mapper.map(1, 1, pageResult);

    assertTrue(collectionDto.getLinks().getLinkBy("create").isPresent());
  }

  @Test
  public void shouldNotHaveCreateLink_whenHasNoPermission() {
    PageResult<Group> pageResult = mockPageResult("nobodies");
    when(subject.isPermitted("group:create")).thenReturn(false);

    CollectionDto collectionDto = mapper.map(1, 1, pageResult);

    assertFalse(collectionDto.getLinks().getLinkBy("create").isPresent());
  }

  @Test
  public void shouldMapGroups() {
    PageResult<Group> pageResult = mockPageResult("nobodies", "bosses");
    CollectionDto collectionDto = mapper.map(1, 2, pageResult);
    List<HalRepresentation> groups = collectionDto.getEmbedded().getItemsBy("groups");
    assertEquals(2, groups.size());
    assertEquals("nobodies", ((GroupDto) groups.get(0)).getName());
    assertEquals("bosses", ((GroupDto) groups.get(1)).getName());
  }

  private PageResult<Group> mockPageResult(String... groupNames) {
    Collection<Group> groups = createGroups(groupNames);
    return new PageResult<>(groups, groups.size());
  }

  private List<Group> createGroups(String... groupNames) {
    return Arrays.stream(groupNames).map(this::mockGroupWithDto).collect(toList());
  }

  private Group mockGroupWithDto(String groupName) {
    Group group = new Group();
    group.setName(groupName);
    when(groupToDtoMapper.map(group)).thenReturn(createGroupDto(group));
    return group;
  }

  private GroupDto createGroupDto(Group group) {
    GroupDto dto = new GroupDto();
    dto.setName(group.getName());
    return dto;
  }

}
