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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.util.ThreadContext;
import org.assertj.core.util.Lists;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.jboss.resteasy.mock.MockHttpResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.DisplayManager;
import sonia.scm.group.DefaultGroupDisplayManager;
import sonia.scm.group.Group;
import sonia.scm.group.xml.XmlGroupDAO;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespaceManager;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.user.DefaultUserDisplayManager;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;
import sonia.scm.web.RestDispatcher;
import sonia.scm.web.VndMediaType;
import sonia.scm.xml.XmlDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@SubjectAware(configuration = "classpath:sonia/scm/shiro-002.ini")
@RunWith(MockitoJUnitRunner.Silent.class)
public class AutoCompleteResourceTest {

  @Rule
  public final ShiroRule shiroRule = new ShiroRule();

  public static final String URL = "/" + AutoCompleteResource.PATH;
  private final Integer defaultLimit = DisplayManager.DEFAULT_LIMIT;

  private RestDispatcher dispatcher = new RestDispatcher();

  private XmlUserDAO userDao;
  private XmlGroupDAO groupDao;
  @Mock
  private NamespaceManager namespaceManager;
  private XmlDatabase xmlDB;
  private ObjectMapper jsonObjectMapper = new ObjectMapper();

  @Before
  public void prepareEnvironment() {
    initMocks(this);
    ConfigurationStoreFactory storeFactory = mock(ConfigurationStoreFactory.class);
    ConfigurationStore<Object> storeConfig = mock(ConfigurationStore.class);
    xmlDB = mock(XmlDatabase.class);
    when(storeConfig.get()).thenReturn(xmlDB);
    when(storeFactory.getStore(any())).thenReturn(storeConfig);
    when(storeFactory.withType(any())).thenCallRealMethod();
    XmlUserDAO userDao = new XmlUserDAO(storeFactory);
    this.userDao = spy(userDao);
    XmlGroupDAO groupDAO = new XmlGroupDAO(storeFactory);
    groupDao = spy(groupDAO);
    ReducedObjectModelToDtoMapperImpl mapper = new ReducedObjectModelToDtoMapperImpl();
    DefaultUserDisplayManager userManager = new DefaultUserDisplayManager(this.userDao);
    DefaultGroupDisplayManager groupManager = new DefaultGroupDisplayManager(groupDao);
    AutoCompleteResource autoCompleteResource = new AutoCompleteResource(mapper, userManager, groupManager, namespaceManager);
    dispatcher.addSingletonResource(autoCompleteResource);
  }

  @After
  public void cleanupContext() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldGet400IfParameterLengthLessThan2CharsForUserSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "users?q=a")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldGet400OnFailedParameterForUserSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "users")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldSearchUsers() throws Exception {
    ArrayList<User> users = Lists.newArrayList(createMockUser("YuCantFindMe", "ha ha"), createMockUser("user1", "User 1"), createMockUser("user2", "User 2"));
    String searched = "user";
    when(xmlDB.values()).thenReturn(users);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "users?q=" + searched)
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
  @SubjectAware(username = "user_without_autocomplete_permission", password = "secret")
  public void shouldGet403OnAutoCompleteUsers() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "users?q=user" )
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }


  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldSearchUsersWithDefaultLimitLength() throws Exception {
    List<User> userList = IntStream.range(0, 10).boxed().map(i -> createMockUser("user" + i, "User " + i)).collect(Collectors.toList());
    when(xmlDB.values()).thenReturn(userList);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "users?q=user")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertResultSize(response, defaultLimit);
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldGet400OnFailedParameterForGroupSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "groups")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldGet400IfParameterLengthLessThan2CharsForGroupSearch() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "groups?q=a")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(400, response.getStatus());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldSearchGroups() throws Exception {
    ArrayList<Group> groups = Lists.newArrayList(createMockGroup("YuCantFindMe"), createMockGroup("group_1"), createMockGroup("group_2"));
    String searched = "group";
    when(xmlDB.values()).thenReturn(groups);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "groups?q=" + searched)
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
  @SubjectAware(username = "user_without_autocomplete_permission", password = "secret")
  public void shouldGet403OnAutoCompleteGroups() throws Exception {
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "groups?q=user" )
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
  }

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void shouldSearchGroupsWithDefaultLimitLength() throws Exception {
    List<Group> groups = IntStream.range(0, 10).boxed().map(i -> createMockGroup("group_" + i)).collect(Collectors.toList());
    when(xmlDB.values()).thenReturn(groups);
    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "groups?q=group")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertResultSize(response, defaultLimit);
  }

  @Test
  @SubjectAware(username = "user_without_autocomplete_permission", password = "secret")
  public void shouldSearchNamespacesForAllUsers() throws Exception {
    when(namespaceManager.getAll()).thenReturn(asList(new Namespace("hog"), new Namespace("hitchhiker")));

    MockHttpRequest request = MockHttpRequest
      .get("/" + AutoCompleteResource.PATH + "namespaces?q=hi")
      .contentType(VndMediaType.AUTOCOMPLETE)
      .accept(VndMediaType.AUTOCOMPLETE);
    MockHttpResponse response = new MockHttpResponse();

    dispatcher.invoke(request, response);

    assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    assertResultSize(response, 1);
    assertTrue(response.getContentAsString().contains("\"id\":\"hitchhiker\""));
  }

  private User createMockUser(String id, String name) {
    return new User(id, name, "em@l.de");
  }

  private Group createMockGroup(String name) {
    Group group = new Group("type", name);
    group.setDescription(name);
    return group;
  }

  private void assertResultSize(MockHttpResponse response, int size) throws java.io.IOException {
    ReducedObjectModelDto[] reducedObjectModelDtos = jsonObjectMapper.readValue(response.getContentAsString(), ReducedObjectModelDto[].class);
    assertEquals(reducedObjectModelDtos.length, size);
  }
}
