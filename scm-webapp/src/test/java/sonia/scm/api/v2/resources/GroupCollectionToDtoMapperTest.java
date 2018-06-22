package sonia.scm.api.v2.resources;

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

import javax.ws.rs.core.UriInfo;
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

public class GroupCollectionToDtoMapperTest {

  private final UriInfo uriInfo = mock(UriInfo.class);
  private final UriInfoStore uriInfoStore = new UriInfoStore();
  private final GroupToGroupDtoMapper groupToDtoMapper = mock(GroupToGroupDtoMapper.class);
  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private final GroupCollectionToDtoMapper mapper = new GroupCollectionToDtoMapper(groupToDtoMapper, uriInfoStore);

  private URI expectedBaseUri;

  @Before
  public void init() throws URISyntaxException {
    uriInfoStore.set(uriInfo);
    URI baseUri = new URI("http://example.com/base/");
    expectedBaseUri = baseUri.resolve(GroupRootResource.GROUPS_PATH_V2 + "/");
    when(uriInfo.getBaseUri()).thenReturn(baseUri);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldSetPageNumber() {
    PageResult<Group> pageResult = mockPageResult(true, "nobodies");
    GroupCollectionDto groupCollectionDto = mapper.map(1, 1, pageResult);
    assertEquals(1, groupCollectionDto.getPage());
  }

  @Test
  public void shouldHaveSelfLink() {
    PageResult<Group> pageResult = mockPageResult(true, "nobodies");
    GroupCollectionDto groupCollectionDto = mapper.map(1, 1, pageResult);
    assertTrue(groupCollectionDto.getLinks().getLinkBy("self").get().getHref().startsWith(expectedBaseUri.toString()));
  }

  @Test
  public void shouldCreateNextPageLink_whenHasMore() {
    PageResult<Group> pageResult = mockPageResult(true, "nobodies");
    GroupCollectionDto groupCollectionDto = mapper.map(1, 1, pageResult);
    assertTrue(groupCollectionDto.getLinks().getLinkBy("next").get().getHref().contains("page=2"));
  }

  @Test
  public void shouldNotCreateNextPageLink_whenNoMore() {
    PageResult<Group> pageResult = mockPageResult(false, "nobodies");
    GroupCollectionDto groupCollectionDto = mapper.map(1, 1, pageResult);
    assertFalse(groupCollectionDto.getLinks().stream().anyMatch(link -> link.getHref().contains("page=2")));
  }

  @Test
  public void shouldHaveCreateLink_whenHasPermission() {
    PageResult<Group> pageResult = mockPageResult(false, "nobodies");
    when(subject.isPermitted("group:create")).thenReturn(true);

    GroupCollectionDto groupCollectionDto = mapper.map(1, 1, pageResult);

    assertTrue(groupCollectionDto.getLinks().getLinkBy("create").isPresent());
  }

  @Test
  public void shouldNotHaveCreateLink_whenHasNoPermission() {
    PageResult<Group> pageResult = mockPageResult(false, "nobodies");
    when(subject.isPermitted("group:create")).thenReturn(false);

    GroupCollectionDto groupCollectionDto = mapper.map(1, 1, pageResult);

    assertFalse(groupCollectionDto.getLinks().getLinkBy("create").isPresent());
  }

  @Test
  public void shouldMapGroups() {
    PageResult<Group> pageResult = mockPageResult(false, "nobodies", "bosses");
    GroupCollectionDto groupCollectionDto = mapper.map(1, 2, pageResult);
    List<HalRepresentation> groups = groupCollectionDto.getEmbedded().getItemsBy("groups");
    assertEquals(2, groups.size());
    assertEquals("nobodies", ((GroupDto) groups.get(0)).getName());
    assertEquals("bosses", ((GroupDto) groups.get(1)).getName());
  }

  private PageResult<Group> mockPageResult(boolean hasMore, String... groupNames) {
    Collection<Group> groups = Arrays.stream(groupNames).map(this::mockGroupWithDto).collect(toList());
    return new PageResult<>(groups, hasMore);
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
