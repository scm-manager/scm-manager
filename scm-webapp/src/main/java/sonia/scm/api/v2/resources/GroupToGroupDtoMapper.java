package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class GroupToGroupDtoMapper extends BaseMapper<Group, GroupDto> {

  @Inject
  private ResourceLinks resourceLinks;

  @AfterMapping
  void appendLinks(Group group, @MappingTarget GroupDto target) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.group().self(target.getName()));
    if (GroupPermissions.delete(group).isPermitted()) {
      linksBuilder.single(link("delete", resourceLinks.group().delete(target.getName())));
    }
    if (GroupPermissions.modify(group).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.group().update(target.getName())));
    }
    target.add(linksBuilder.build());
  }

  @AfterMapping
  void mapMembers(Group group, @MappingTarget GroupDto target) {
    List<MemberDto> memberDtos = group.getMembers().stream().map(this::createMember).collect(Collectors.toList());
    target.withMembers(memberDtos);
  }

  private MemberDto createMember(String name) {
    Links.Builder linksBuilder = linkingTo().self(resourceLinks.user().self(name));
    MemberDto memberDto = new MemberDto(name);
    memberDto.add(linksBuilder.build());
    return memberDto;
  }
}
