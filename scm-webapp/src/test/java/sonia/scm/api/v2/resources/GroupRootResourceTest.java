package sonia.scm.api.v2.resources;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import com.google.common.io.Resources;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.PageResult;
import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class GroupRootResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  @Mock
  private ResourceLinks resourceLinks;

  @Mock
  private GroupManager groupManager;
  @InjectMocks
  private GroupDtoToGroupMapperImpl dtoToGroupMapper;
  @InjectMocks
  private GroupToGroupDtoMapperImpl groupToDtoMapper;

  private ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);

  @Before
  public void prepareEnvironment() throws IOException, GroupException {
    initMocks(this);
    doNothing().when(groupManager).create(groupCaptor.capture());
    doNothing().when(groupManager).modify(groupCaptor.capture());

    Group group = createDummyGroup();
    when(groupManager.getPage(any(), eq(0), eq(10))).thenReturn(new PageResult<>(singletonList(group), 1));
    when(groupManager.get("admin")).thenReturn(group);

    ResourceLinksMock.initMock(resourceLinks, URI.create("/"));

    GroupCollectionToDtoMapper groupCollectionToDtoMapper = new GroupCollectionToDtoMapper(groupToDtoMapper, resourceLinks);
    GroupCollectionResource groupCollectionResource = new GroupCollectionResource(groupManager, dtoToGroupMapper, groupCollectionToDtoMapper, resourceLinks);
    GroupResource groupResource = new GroupResource(groupManager, groupToDtoMapper, dtoToGroupMapper);
    GroupRootResource groupRootResource = new GroupRootResource(MockProvider.of(groupCollectionResource), MockProvider.of(groupResource));

    dispatcher.getRegistry().addSingletonResource(groupRootResource);
  }

  @Test
  public void shouldGetNotFoundForNotExistentGroup() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + GroupRootResource.GROUPS_PATH_V2 + "nosuchgroup");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldGetGroup() throws URISyntaxException {
    Group group = createDummyGroup();
    when(groupManager.get("admin")).thenReturn(group);

    MockHttpRequest request = MockHttpRequest.get("/" + GroupRootResource.GROUPS_PATH_V2 + "admin");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"admin\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/groups/admin\"}"));
    assertTrue(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/groups/admin\"}"));
    assertTrue(response.getContentAsString().contains("\"name\":\"user\""));
  }

  @Test
  public void shouldUpdateGroup() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/group-test-update.json");
    byte[] groupJson = Resources.toByteArray(url);

    Group group = createDummyGroup();
    when(groupManager.get("admin")).thenReturn(group);
    Group updatedGroup = createDummyGroup();
    updatedGroup.setDescription("Updated description");

    MockHttpRequest request = MockHttpRequest
      .put("/" + GroupRootResource.GROUPS_PATH_V2 + "admin")
      .contentType(VndMediaType.GROUP)
      .content(groupJson);

    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);
    Group capturedGroup = groupCaptor.getValue();

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());

    assertEquals("Updated description", capturedGroup.getDescription());
  }

  @Test
  public void updateShouldFailOnNonexistentGroup() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/group-test-update.json");
    byte[] groupJson = Resources.toByteArray(url);

    Group updatedGroup = createDummyGroup();
    updatedGroup.setDescription("Updated description");

    MockHttpRequest request = MockHttpRequest
      .put("/" + GroupRootResource.GROUPS_PATH_V2 + "idontexist")
      .contentType(VndMediaType.GROUP)
      .content(groupJson);

    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldDeleteGroup() throws URISyntaxException {
    Group group = createDummyGroup();
    when(groupManager.get("admin")).thenReturn(group);

    MockHttpRequest request = MockHttpRequest.delete("/" + GroupRootResource.GROUPS_PATH_V2 + "admin");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  @Test
  public void shouldNotFailOnDeletingNonexistentGroup() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.delete("/" + GroupRootResource.GROUPS_PATH_V2 + "idontexist");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
  }

  @Test
  public void shouldCreateNewGroupWithMembers() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/group-test-create.json");
    byte[] groupJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + GroupRootResource.GROUPS_PATH_V2)
      .contentType(VndMediaType.GROUP)
      .content(groupJson);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(201, response.getStatus());
    Group createdGroup = groupCaptor.getValue();
    assertNotNull(createdGroup);
    assertEquals(2, createdGroup.getMembers().size());
    assertEquals("user1", createdGroup.getMembers().get(0));
  }

  @Test
  public void shouldFailForMissingContent() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest
      .post("/" + GroupRootResource.GROUPS_PATH_V2)
      .contentType(VndMediaType.GROUP)
      .content(new byte[] {});
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldGetAll() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + GroupRootResource.GROUPS_PATH_V2);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"admin\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/groups/admin\"}"));
  }

  private Group createDummyGroup() {
    Group group = new Group();
    group.setName("admin");
    group.setCreationDate(0L);
    group.setMembers(Collections.singletonList("user"));
    return group;
  }
}
