package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.assertj.core.util.Lists;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.SCMContextProvider;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.DefaultGroupManager;
import sonia.scm.group.Group;
import sonia.scm.group.GroupManager;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.repository.DefaultRepositoryManager;
import sonia.scm.repository.NamespaceStrategy;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import sonia.scm.repository.xml.XmlRepositoryDAO;
import sonia.scm.security.KeyGenerator;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.user.DefaultUserManager;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.web.VndMediaType;
import sonia.scm.xml.XmlDatabase;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static sonia.scm.api.v2.resources.DispatcherMock.createDispatcher;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AutoCompleteResourceTest {

  public static final String URL = "/" + AutoCompleteResource.PATH;
  private final Integer defaultLimit = Integer.valueOf(AutoCompleteResource.DEFAULT_LIMIT);
  private Dispatcher dispatcher;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);


  private XmlUserDAO userDaoMock;

  private XmlGroupDAO groupDaoMock;

  private XmlRepositoryDAO repoDaoMock;
  private XmlDatabase xmlDB;
  private ObjectMapper jsonObjectMapper = new ObjectMapper();


  @Before
  public void prepareEnvironment() {
    initMocks(this);
    ConfigurationStoreFactory storeFactory = mock(ConfigurationStoreFactory.class);
    ConfigurationStore<Object> storeConfig = mock(ConfigurationStore.class);
    xmlDB = mock(XmlDatabase.class);
    when(storeConfig.get()).thenReturn(xmlDB);
    when(storeFactory.getStore(any(), any())).thenReturn(storeConfig);
    XmlUserDAO userDao = new XmlUserDAO(storeFactory);
    userDaoMock = spy(userDao);
    XmlGroupDAO groupDAO = new XmlGroupDAO(storeFactory);
    groupDaoMock = spy(groupDAO);
    XmlRepositoryDAO repoDao = new XmlRepositoryDAO(storeFactory);
    repoDaoMock = spy(repoDao);
    ReducedObjectModelToDtoMapperImpl mapper = new ReducedObjectModelToDtoMapperImpl();
    UserManager userManager = new DefaultUserManager(userDaoMock);
    GroupManager groupManager = new DefaultGroupManager(groupDaoMock);
    RepositoryManager repositoryManager = new DefaultRepositoryManager(mock(ScmConfiguration.class), mock(SCMContextProvider.class), mock(KeyGenerator.class), repoDaoMock, new HashSet<>(), mock(NamespaceStrategy.class));
    AutoCompleteResource autoCompleteResource = new AutoCompleteResource(mapper, userManager, groupManager, repositoryManager);
    dispatcher = createDispatcher(autoCompleteResource);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
    when(subject.isPermitted(any(String.class))).thenReturn(true);
  }

  @After
  public void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldGet400OnFailedParameterForUserSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "user")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldGet400IfParameterLengthLessThan2CharsForUserSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "user?filter=a")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldSearchUsers() throws Exception {
    ArrayList<User> users = Lists.newArrayList(createMockUser("YuCantFindMe", "ha ha"), createMockUser("user1", "User 1"), createMockUser("user2", "User 2"));
    String searched = "user";
    when(xmlDB.values()).thenReturn(users);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "user?filter=" + searched)
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());

    assertResultSize(response, 2);
    assertTrue(response.getContentAsString().contains("\"id\":\"user1\""));
    assertTrue(response.getContentAsString().contains("\"displayName\":\"User 1\""));
    assertTrue(response.getContentAsString().contains("\"id\":\"user2\""));
    assertTrue(response.getContentAsString().contains("\"displayName\":\"User 2\""));
  }

  @Test
  public void shouldSearchUsersWithLimitLength() throws Exception {
    List<User> users = IntStream.range(0, 10).boxed().map(i -> createMockUser("user" + i, "User " + i)).collect(Collectors.toList());
    when(xmlDB.values()).thenReturn(users);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "user?filter=user&limit=1")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    verify(userDaoMock).getFiltered(eq("user"), eq(1));
    assertResultSize(response, 1);
  }

  @Test
  public void shouldSearchUsersWithDefaultLimitLength() throws Exception {
    List<User> userList = IntStream.range(0, 10).boxed().map(i -> createMockUser("user" + i, "User " + i)).collect(Collectors.toList());
    when(xmlDB.values()).thenReturn(userList);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "user?filter=user")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    verify(userDaoMock).getFiltered(eq("user"), eq(defaultLimit));
    assertResultSize(response, defaultLimit);
  }

  @Test
  public void shouldGet400OnFailedParameterForGroupSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "group")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldGet400IfParameterLengthLessThan2CharsForGroupSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "group?filter=a")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldSearchGroups() throws Exception {
    ArrayList<Group> groups = Lists.newArrayList(createMockGroup("YuCantFindMe"), createMockGroup("group_1"), createMockGroup("group_2"));
    String searched = "group";
    when(xmlDB.values()).thenReturn(groups);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "group?filter=" + searched)
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertResultSize(response, 2);
    assertTrue(response.getContentAsString().contains("\"id\":\"group_1\""));
    assertTrue(response.getContentAsString().contains("\"displayName\":\"group_1\""));
    assertTrue(response.getContentAsString().contains("\"id\":\"group_2\""));
    assertTrue(response.getContentAsString().contains("\"displayName\":\"group_2\""));
  }

  @Test
  public void shouldSearchGroupsWithLimitLength() throws Exception {
    List<Group> groups = IntStream.range(0, 10).boxed().map(i -> createMockGroup("group_" + i)).collect(Collectors.toList());
    when(xmlDB.values()).thenReturn(groups);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "group?filter=group&limit=1")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    verify(groupDaoMock).getFiltered(eq("group"), eq(1));
    assertResultSize(response, 1);
  }

  @Test
  public void shouldSearchGroupsWithDefaultLimitLength() throws Exception {
    List<Group> groups = IntStream.range(0, 10).boxed().map(i -> createMockGroup("group_" + i)).collect(Collectors.toList());
    when(xmlDB.values()).thenReturn(groups);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "group?filter=group")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    verify(groupDaoMock).getFiltered(eq("group"), eq(defaultLimit));
    assertResultSize(response, defaultLimit);
  }


  @Test
  public void shouldGet400OnFailedParameterForRepoSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "repository")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldGet400IfParameterLengthLessThan2CharsForRepoSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "repository?filter=a")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  public void shouldSearchRepos() throws Exception {
    List<Repository> repos = Lists.newArrayList(createMockRepo("YCannotFindMe"), createMockRepo("repo1"), createMockRepo("repo2"));
    when(xmlDB.values()).thenReturn(repos);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "repository?filter=repo")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertResultSize(response, 2);
    assertTrue(response.getContentAsString().contains("\"displayName\":\"space/repo1\""));
    assertTrue(response.getContentAsString().contains("\"displayName\":\"space/repo2\""));
  }

  @Test
  public void shouldSearchReposWithLimitLength() throws Exception {
    List<Repository> repositories = IntStream.range(0, 10).boxed().map(i -> createMockRepo("repo" + i)).collect(Collectors.toList());
    when(xmlDB.values()).thenReturn(repositories);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "repository?filter=repo&limit=1")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    verify(repoDaoMock).getFiltered(eq("repo"), eq(1));
    assertResultSize(response, 1);
  }

  @Test
  public void shouldSearchReposWithDefaultLimitLength() throws Exception {
    List<Repository> repositories = IntStream.range(0, 10).boxed().map(i -> createMockRepo("repo" + i)).collect(Collectors.toList());
    when(xmlDB.values()).thenReturn(repositories);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "repository?filter=repo")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    verify(repoDaoMock).getFiltered(eq("repo"), eq(defaultLimit));
    assertResultSize(response, defaultLimit);
  }


  private User createMockUser(String id, String name) {
    return new User(id, name, "em@l.de");
  }


  private Group createMockGroup(String name) {
    Group group = new Group("type", name);
    group.setDescription(name);
    return group;
  }

  private Repository createMockRepo(String repository) {
    return new Repository("id", "git", "space", repository);
  }

  private void assertResultSize(MockHttpResponse response, int size) throws java.io.IOException {
    ReducedObjectModelDto[] reducedObjectModelDtos = jsonObjectMapper.readValue(response.getContentAsString(), ReducedObjectModelDto[].class);
    assertTrue(reducedObjectModelDtos.length == size);
  }
}
