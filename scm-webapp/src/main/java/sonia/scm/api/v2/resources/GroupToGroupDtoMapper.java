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
import static sonia.scm.api.v2.resources.ResourceLinks.group;
import static sonia.scm.api.v2.resources.ResourceLinks.user;

@Mapper
public abstract class GroupToGroupDtoMapper extends BaseMapper<Group, GroupDto> {

  @Inject
  private UriInfoStore uriInfoStore;

  @AfterMapping
  void appendLinks(Group group, @MappingTarget GroupDto target) {
    Links.Builder linksBuilder = linkingTo().self(group(uriInfoStore.get()).self(target.getName()));
    if (GroupPermissions.delete(group).isPermitted()) {
      linksBuilder.single(link("delete", group(uriInfoStore.get()).delete(target.getName())));
    }
    if (GroupPermissions.modify(group).isPermitted()) {
      linksBuilder.single(link("update", group(uriInfoStore.get()).update(target.getName())));
    }
    target.add(linksBuilder.build());
  }

  @AfterMapping
  void mapMembers(Group group, @MappingTarget GroupDto target) {
    List<MemberDto> memberDtos = group.getMembers().stream().map(name -> this.createMember(name)).collect(Collectors.toList());
    target.withEmbedded("members", memberDtos);
  }

  private MemberDto createMember(String name) {
    Links.Builder linksBuilder = linkingTo().self(user(uriInfoStore.get()).self(name));
    MemberDto memberDto = new MemberDto(name);
    memberDto.add(linksBuilder.build());
    return memberDto;
  }
}
