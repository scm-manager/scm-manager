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
import sonia.scm.group.Group;
import sonia.scm.group.GroupException;
import sonia.scm.group.GroupManager;
import sonia.scm.web.VndMediaType;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(
  username = "trillian",
  password = "secret",
  configuration = "classpath:sonia/scm/repository/shiro.ini"
)
public class GroupV2ResourceTest {

  @Rule
  public ShiroRule shiro = new ShiroRule();

  private Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();

  @Mock
  private GroupManager groupManager;
  private final UriInfoStore uriInfoStore = new UriInfoStore();
  @InjectMocks
  GroupDtoToGroupMapperImpl dtoToGroupMapper;
  @InjectMocks
  GroupToGroupDtoMapperImpl groupToDtoMapper;
  @InjectMocks
  GroupCollectionToDtoMapper groupCollectionToDtoMapper;


  ArgumentCaptor<Group> groupCaptor = ArgumentCaptor.forClass(Group.class);

  @Before
  public void prepareEnvironment() throws IOException, GroupException {
    initMocks(this);
    doNothing().when(groupManager).create(groupCaptor.capture());

    Group group = new Group();
    group.setName("admin");
    group.setCreationDate(0L);
    group.setMembers(Collections.singletonList("user"));
    when(groupManager.get("admin")).thenReturn(group);

    GroupCollectionResource groupCollectionResource = new GroupCollectionResource(groupManager, dtoToGroupMapper, groupToDtoMapper, groupCollectionToDtoMapper);
    GroupSubResource groupSubResource = new GroupSubResource(groupManager, groupToDtoMapper);
    GroupV2Resource groupV2Resource = new GroupV2Resource(groupCollectionResource, groupSubResource);

    dispatcher.getRegistry().addSingletonResource(groupV2Resource);
  }

  @Test
  public void shouldGetNotFoundForNotExistentGroup() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + GroupV2Resource.GROUPS_PATH_V2 + "nosuchgroup");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  @SubjectAware(username = "unpriv")
  public void shouldGetNotAuthorizedForWrongUser() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/" + GroupV2Resource.GROUPS_PATH_V2 + "admin");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  public void shouldGetGroup() throws URISyntaxException {
    Group group = new Group();
    group.setName("admin");
    group.setCreationDate(0L);
    group.setMembers(Collections.singletonList("user"));
    when(groupManager.get("admin")).thenReturn(group);

    MockHttpRequest request = MockHttpRequest.get("/" + GroupV2Resource.GROUPS_PATH_V2 + "admin");
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertTrue(response.getContentAsString().contains("\"name\":\"admin\""));
    assertTrue(response.getContentAsString().contains("\"self\":{\"href\":\"/v2/groups/admin\"}"));
    assertTrue(response.getContentAsString().contains("\"delete\":{\"href\":\"/v2/groups/admin\"}"));
    assertTrue(response.getContentAsString().contains("\"name\":\"user\""));
  }

  @Test
  public void shouldCreateNewGroupWithMembers() throws URISyntaxException, IOException {
    URL url = Resources.getResource("sonia/scm/api/v2/group-test-create.json");
    byte[] groupJson = Resources.toByteArray(url);

    MockHttpRequest request = MockHttpRequest
      .post("/" + GroupV2Resource.GROUPS_PATH_V2)
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
}
