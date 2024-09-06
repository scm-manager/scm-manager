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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;
import sonia.scm.security.PermissionPermissions;
import sonia.scm.user.UserManager;
import sonia.scm.web.EdisonHalAppender;

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
