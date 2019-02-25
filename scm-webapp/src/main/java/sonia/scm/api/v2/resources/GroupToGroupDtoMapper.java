package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;
import sonia.scm.security.PermissionPermissions;

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
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.user().self(name));
    MemberDto memberDto = new MemberDto(name);
    memberDto.add(linksBuilder.build());
    return memberDto;
  }
}
