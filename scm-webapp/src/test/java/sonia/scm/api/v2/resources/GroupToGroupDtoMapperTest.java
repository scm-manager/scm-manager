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

import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadContext;
import org.apache.shiro.util.ThreadState;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import sonia.scm.group.Group;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import java.net.URI;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class GroupToGroupDtoMapperTest {

  private final URI baseUri = URI.create("http://example.com/base/");
  @SuppressWarnings("unused") // Is injected
  private final ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);

  @Mock
  private UserManager userManager;
  @InjectMocks
  private GroupToGroupDtoMapperImpl mapper;

  private final Subject subject = mock(Subject.class);
  private final ThreadState subjectThreadState = new SubjectThreadState(subject);

  private URI expectedBaseUri;

  @Before
  public void init() {
    initMocks(this);
    expectedBaseUri = baseUri.resolve(GroupRootResource.GROUPS_PATH_V2 + "/");
    subjectThreadState.bind();
    ThreadContext.bind(subject);
  }

  @After
  public void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  public void shouldMapAttributes() {
    Group group = createDefaultGroup();

    GroupDto groupDto = mapper.map(group);

    assertEquals("abc", groupDto.getName());
    assertEquals("abc", groupDto.getName());
  }

  @Test
  public void shouldMapSelfLink() {
    Group group = createDefaultGroup();

    GroupDto groupDto = mapper.map(group);

    assertEquals("expected self link", expectedBaseUri.resolve("abc").toString(), groupDto.getLinks().getLinkBy("self").get().getHref());
  }

  @Test
  public void shouldMapLink_forUpdate() {
    Group group = createDefaultGroup();
    when(subject.isPermitted("group:modify:abc")).thenReturn(true);

    GroupDto groupDto = mapper.map(group);

    assertEquals("expected update link", expectedBaseUri.resolve("abc").toString(), groupDto.getLinks().getLinkBy("update").get().getHref());
  }

  @Test
  public void shouldCreateMemberDtosWithLinksForExistingUsers() {
    Group group = createDefaultGroup();
    group.setMembers(IntStream.range(0, 10).mapToObj(n -> "user" + n).collect(toList()));
    when(userManager.contains(any())).thenReturn(true);

    GroupDto groupDto = mapper.map(group);

    assertEquals(10, groupDto.getEmbedded().getItemsBy("members").size());
    MemberDto actualMember = (MemberDto) groupDto.getEmbedded().getItemsBy("members").iterator().next();
    assertEquals("user0", actualMember.getName());
    assertEquals("http://example.com/base/v2/users/user0", actualMember.getLinks().getLinkBy("self").get().getHref());
  }

  @Test
  public void shouldCreateMemberDtosWithoutLinksForMissingUsers() {
    Group group = createDefaultGroup();
    group.setMembers(IntStream.range(0, 10).mapToObj(n -> "user" + n).collect(toList()));
    when(userManager.contains(any())).thenReturn(false);

    GroupDto groupDto = mapper.map(group);

    assertEquals(10, groupDto.getEmbedded().getItemsBy("members").size());
    MemberDto actualMember = (MemberDto) groupDto.getEmbedded().getItemsBy("members").iterator().next();
    assertEquals("user0", actualMember.getName());
    assertThat(actualMember.getLinks().getLinkBy("self")).isEmpty();
  }

  @Test
  public void shouldAppendLinks() {
    HalEnricherRegistry registry = new HalEnricherRegistry();
    registry.register(Group.class, (ctx, appender) -> {
      Group group = ctx.oneRequireByType(Group.class);
      appender.appendLink("some", "http://" + group.getName());
    });
    mapper.setRegistry(registry);

    Group group = createDefaultGroup();
    GroupDto dto = mapper.map(group);

    assertEquals("http://abc", dto.getLinks().getLinkBy("some").get().getHref());
  }

  private Group createDefaultGroup() {
    Group group = new Group();
    group.setName("abc");
    group.setCreationDate(1L);
    return group;
  }
}
