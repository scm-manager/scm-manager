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

import de.otto.edison.hal.HalRepresentation;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.PageResult;
import sonia.scm.user.User;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static sonia.scm.PageResult.createPage;

public class UserCollectionToDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);
  @Mock
  private UserToUserDtoMapper userToDtoMapper;
  @Mock
  private Subject subject;

  @InjectMocks
  private SubjectThreadState subjectThreadState;
  private URI expectedBaseUri;

  private UserCollectionToDtoMapper mapper;

  @Before
  public void init() throws URISyntaxException {
    initMocks(this);
    mapper = new UserCollectionToDtoMapper(userToDtoMapper, resourceLinks, Set.of());
    expectedBaseUri = baseUri.resolve(UserRootResource.USERS_PATH_V2 + "/");
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldSetPageNumber() {
    PageResult<User> pageResult = mockPageResult("Hannes");
    CollectionDto collectionDto = mapper.map(0, 1, pageResult);
    assertEquals(0, collectionDto.getPage());
  }

  @Test
  public void shouldHaveSelfLink() {
    PageResult<User> pageResult = mockPageResult("Hannes");
    CollectionDto collectionDto = mapper.map(0, 1, pageResult);
    assertTrue(collectionDto.getLinks().getLinkBy("self").get().getHref().startsWith(expectedBaseUri.toString()));
  }

  @Test
  public void shouldCreateNextPageLink_whenHasMore() {
    PageResult<User> pageResult = createPage(createUsers("Hannes", "Karl"), 0, 1);

    CollectionDto collectionDto = mapper.map(0, 1, pageResult);
    assertTrue(collectionDto.getLinks().getLinkBy("next").get().getHref().contains("page=1"));
  }

  @Test
  public void shouldNotCreateNextPageLink_whenNoMore() {
    PageResult<User> pageResult = mockPageResult("Hannes", "Wurst", "X");
    CollectionDto collectionDto = mapper.map(0, 10, pageResult);
    assertFalse(collectionDto.getLinks().getLinkBy("next").isPresent());
  }

  @Test
  public void shouldHaveCreateLink_whenHasPermission() {
    PageResult<User> pageResult = mockPageResult("Hannes");
    when(subject.isPermitted("user:create")).thenReturn(true);

    CollectionDto collectionDto = mapper.map(0, 1, pageResult);

    assertTrue(collectionDto.getLinks().getLinkBy("create").isPresent());
  }

  @Test
  public void shouldNotHaveCreateLink_whenHasNoPermission() {
    PageResult<User> pageResult = mockPageResult("Hannes");
    when(subject.isPermitted("user:create")).thenReturn(false);

    CollectionDto collectionDto = mapper.map(0, 1, pageResult);

    assertFalse(collectionDto.getLinks().getLinkBy("create").isPresent());
  }

  @Test
  public void shouldMapUsers() {
    PageResult<User> pageResult = mockPageResult("Hannes", "Wurst");
    CollectionDto collectionDto = mapper.map(0, 2, pageResult);
    List<HalRepresentation> users = collectionDto.getEmbedded().getItemsBy("users");
    assertEquals(2, users.size());
    assertEquals("Hannes", ((UserDto) users.get(0)).getName());
    assertEquals("Wurst", ((UserDto) users.get(1)).getName());
  }

  @Test
  public void shouldCreatePageTotal_forSparsePages() {
    PageResult<User> pageResult = createPage(createUsers("Hannes", "Karl", "Piet"), 0, 1);

    CollectionDto collectionDto = mapper.map(0, 2, pageResult);
    assertEquals(2, collectionDto.getPageTotal());
  }

  @Test
  public void shouldCreatePageTotal_forCompletePages() {
    PageResult<User> pageResult = createPage(createUsers("Hannes", "Karl", "Piet", "Hein"), 0, 1);

    CollectionDto collectionDto = mapper.map(0, 2, pageResult);
    assertEquals(2, collectionDto.getPageTotal());
  }

  @Test
  public void shouldCreatePageTotal_forNoPages() {
    PageResult<User> pageResult = createPage(createUsers(), 0, 1);

    CollectionDto collectionDto = mapper.map(0, 1, pageResult);
    assertEquals(0, collectionDto.getPageTotal());
  }

  private PageResult<User> mockPageResult(String... userNames) {
    return createPage(createUsers(userNames), 0, userNames.length);
  }

  private List<User> createUsers(String... userNames) {
    return Arrays.stream(userNames).map(this::mockUserWithDto).collect(toList());
  }

  private User mockUserWithDto(String userName) {
    User user = new User();
    user.setName(userName);
    when(userToDtoMapper.map(user)).thenReturn(createUserDto(user));
    return user;
  }

  private UserDto createUserDto(User user) {
    UserDto dto = new UserDto();
    dto.setName(user.getName());
    return dto;
  }

}
