package sonia.scm.api.v2.resources;

import de.otto.edison.hal.HalRepresentation;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.Before;
import org.junit.Test;
import sonia.scm.PageResult;
import sonia.scm.user.User;

import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserCollection2DtoMapperTest {

  private final UriInfo uriInfo = mock(UriInfo.class);
  private final User2UserDtoMapper userToDtoMapper = mock(User2UserDtoMapper.class);
  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private final UserCollection2DtoMapper mapper = new UserCollection2DtoMapper(userToDtoMapper);

  private URI expectedBaseUri;

  @Before
  public void init() throws URISyntaxException {
    URI baseUri = new URI("http://example.com/base/");
    expectedBaseUri = baseUri.resolve(UserV2Resource.USERS_PATH_V2 + "/");
    when(uriInfo.getBaseUri()).thenReturn(baseUri);
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @Test
  public void shouldSetPageNumber() {
    PageResult<User> pageResult = mockPageResult(true, "Hannes");
    UserCollectionDto userCollectionDto = mapper.userCollectionToUserDto(uriInfo, 1, 1, pageResult);
    assertEquals(1, userCollectionDto.getPage());
  }

  @Test
  public void shouldHaveSelfLink() {
    PageResult<User> pageResult = mockPageResult(true, "Hannes");
    UserCollectionDto userCollectionDto = mapper.userCollectionToUserDto(uriInfo, 1, 1, pageResult);
    assertTrue(userCollectionDto.getLinks().getLinkBy("self").get().getHref().startsWith(expectedBaseUri.toString()));
  }

  @Test
  public void shouldCreateNextPageLink_whenHasMore() {
    PageResult<User> pageResult = mockPageResult(true, "Hannes");
    UserCollectionDto userCollectionDto = mapper.userCollectionToUserDto(uriInfo, 1, 1, pageResult);
    assertTrue(userCollectionDto.getLinks().getLinkBy("next").get().getHref().contains("page=2"));
  }

  @Test
  public void shouldNotCreateNextPageLink_whenNoMore() {
    PageResult<User> pageResult = mockPageResult(false, "Hannes");
    UserCollectionDto userCollectionDto = mapper.userCollectionToUserDto(uriInfo, 1, 1, pageResult);
    assertFalse(userCollectionDto.getLinks().stream().anyMatch(link -> link.getHref().contains("page=2")));
  }

  @Test
  public void shouldHaveCreateLink_whenHasPermission() {
    PageResult<User> pageResult = mockPageResult(false, "Hannes");
    when(subject.isPermitted("user:create")).thenReturn(true);

    UserCollectionDto userCollectionDto = mapper.userCollectionToUserDto(uriInfo, 1, 1, pageResult);

    assertTrue(userCollectionDto.getLinks().getLinkBy("create").isPresent());
  }

  @Test
  public void shouldNotHaveCreateLink_whenHasNoPermission() {
    PageResult<User> pageResult = mockPageResult(false, "Hannes");
    when(subject.isPermitted("user:create")).thenReturn(false);

    UserCollectionDto userCollectionDto = mapper.userCollectionToUserDto(uriInfo, 1, 1, pageResult);

    assertFalse(userCollectionDto.getLinks().getLinkBy("create").isPresent());
  }

  @Test
  public void shouldMapUsers() {
    PageResult<User> pageResult = mockPageResult(false, "Hannes", "Wurst");
    UserCollectionDto userCollectionDto = mapper.userCollectionToUserDto(uriInfo, 1, 2, pageResult);
    List<HalRepresentation> users = userCollectionDto.getEmbedded().getItemsBy("users");
    assertEquals(2, users.size());
    assertEquals("Hannes", ((UserDto) users.get(0)).getName());
    assertEquals("Wurst", ((UserDto) users.get(1)).getName());
  }

  private PageResult<User> mockPageResult(boolean hasMore, String... userNames) {
    Collection<User> users = Arrays.stream(userNames).map(this::mockUserWithDto).collect(toList());
    return new PageResult<>(users, hasMore);
  }

  private User mockUserWithDto(String userName) {
    User user = new User();
    user.setName(userName);
    when(userToDtoMapper.userToUserDto(user, uriInfo)).thenReturn(createUserDto(user));
    return user;
  }

  private UserDto createUserDto(User user) {
    UserDto dto = new UserDto();
    dto.setName(user.getName());
    return dto;
  }

}
