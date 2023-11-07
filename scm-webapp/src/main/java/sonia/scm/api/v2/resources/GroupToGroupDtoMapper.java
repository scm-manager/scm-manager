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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.UserManager;
import sonia.scm.web.EdisonHalAppender;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class GroupToGroupDtoMapper extends BaseMapper<Group, GroupDto> {

  @Inject
  private ResourceLinks resourceLinks;
  @Inject
  private UserManager userManager;

  @ObjectFactory
  GroupDto createDto(Group group) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.group().self(group.getName()));
    if (GroupPermissions.delete(group).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.group().delete(group.getName())));
    }
    if (GroupPermissions.modify(group).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.group().update(group.getName())));
    }
    if (PermissionPermissions.read().isPermitted()) {
      linksBuilder.single(link("permissions", resourceLinks.groupPermissions().permissions(group.getName())));
    }

    Embedded.Builder embeddedBuilder = embeddedBuilder();
    List<MemberDto> memberDtos = group.getMembers().stream().map(this::createMember).collect(Collectors.toList());
    embeddedBuilder.with("members", memberDtos);

    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), group);

    return new GroupDto(linksBuilder.build(), embeddedBuilder.build());
  }

  private MemberDto createMember(String name) {
    MemberDto memberDto = new MemberDto(name);
    if (userManager.contains(name)) {
      Links.Builder linksBuilder = linkingTo().self(resourceLinks.user().self(name));
      memberDto.add(linksBuilder.build());
    }
    return memberDto;
  }
}
