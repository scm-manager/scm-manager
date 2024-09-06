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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.admin.ScmConfigurationStore;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;
import sonia.scm.user.UserTestData;

import java.net.URI;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class UserToUserDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private UserManager userManager;
  @Mock
  private ScmConfigurationStore scmConfigurationStore;
  @Mock
  private ScmConfiguration scmConfiguration;

  @InjectMocks
  private UserToUserDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    initMocks(this);
    when(userManager.getDefaultType()).thenReturn("xml");
    expectedBaseUri = baseUri.resolve(UserRootResource.USERS_PATH_V2 + "/");
    subjectThreadState.bind();
    ThreadContext.bind(subject);

    when(scmConfigurationStore.get()).thenReturn(scmConfiguration);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapLinks_forUpdate() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);
    assertEquals("expected self link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("self").get().getHref());
    assertEquals("expected update link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldGetInternalUserLinks() {
    User user = createDefaultUser();
    user.setExternal(false);
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertEquals("expected password link with modify permission", expectedBaseUri.resolve("abc/password").toString(), userDto.getLinks().getLinkBy("password").get().getHref());
    assertEquals("expected convert to external link with modify permission", expectedBaseUri.resolve("abc/convert-to-external").toString(), userDto.getLinks().getLinkBy("convertToExternal").get().getHref());
  }

  @Test
  public void shouldGetEmptyPasswordProperty() {
    User user = createDefaultUser();
    user.setPassword("myHighSecurePassword");
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertThat(userDto.getPassword()).isBlank();
  }

  @Test
  public void shouldMapLinks_forDelete() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:delete:abc")).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertEquals("expected self link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("self").get().getHref());
    assertEquals("expected delete link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("delete").get().getHref());
  }

  private User createDefaultUser() {
    User user = new User();
    user.setName("abc");
    user.setCreationDate(1L);
    return user;
  }

  @Test
  public void shouldMapLinks_forNormalUser() {
    User user = createDefaultUser();
    when(subject.hasRole("user")).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertEquals("expected self link", expectedBaseUri.resolve("abc").toString(), userDto.getLinks().getLinkBy("self").get().getHref());
    assertFalse("expected no delete link", userDto.getLinks().getLinkBy("delete").isPresent());
    assertFalse("expected no update link", userDto.getLinks().getLinkBy("update").isPresent());
  }

  @Test
  public void shouldMapFields() {
    User user = createDefaultUser();

    UserDto userDto = mapper.map(user);

    assertEquals("abc", userDto.getName());
  }

  @Test
  public void shouldMapTimes() {
    User user = createDefaultUser();
    Instant expectedCreationDate = Instant.ofEpochSecond(6666666);
    Instant expectedModificationDate = expectedCreationDate.plusSeconds(1);
    user.setCreationDate(expectedCreationDate.toEpochMilli());
    user.setLastModified(expectedModificationDate.toEpochMilli());

    UserDto userDto = mapper.map(user);

    assertEquals(expectedCreationDate, userDto.getCreationDate());
    assertEquals(expectedModificationDate, userDto.getLastModified());
  }

  @Test
  public void shouldAppendLink() {
    User trillian = UserTestData.createTrillian();

    HalEnricherRegistry registry = new HalEnricherRegistry();
    registry.register(User.class, (ctx, appender) -> appender.appendLink("sample", "http://" + ctx.oneByType(User.class).get().getName()));
    mapper.setRegistry(registry);

    UserDto userDto = mapper.map(trillian);

    assertEquals("http://trillian", userDto.getLinks().getLinkBy("sample").get().getHref());
  }

  @Test
  public void shouldMapLinks_forPermissionOverview() {
    User user = createDefaultUser();
    when(subject.isPermitted("permission:read")).thenReturn(true);
    when(subject.isPermitted("group:list")).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertEquals("expected permissions link", expectedBaseUri.resolve("abc/permissions").toString(), userDto.getLinks().getLinkBy("permissions").get().getHref());
    assertEquals("expected permission overview link", expectedBaseUri.resolve("abc/permissionOverview").toString(), userDto.getLinks().getLinkBy("permissionOverview").get().getHref());
  }

  @Test
  public void shouldMapApiKeyLinks_IfEnabled() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);
    when(scmConfiguration.isEnabledApiKeys()).thenReturn(true);

    UserDto userDto = mapper.map(user);

    assertEquals("expected api key link", expectedBaseUri.resolve("abc/api_keys").toString(), userDto.getLinks().getLinkBy("apiKeys").get().getHref());
  }

  @Test
  public void shouldNotMapApiKeyLinks_IfDisabled() {
    User user = createDefaultUser();
    when(subject.isPermitted("user:modify:abc")).thenReturn(true);
    when(scmConfiguration.isEnabledApiKeys()).thenReturn(false);

    UserDto userDto = mapper.map(user);

    assertThat(userDto.getLinks().getLinkBy("apiKeys")).isEmpty();
  }
}
