package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;

import javax.ws.rs.core.UriInfo;
import java.util.List;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.api.v2.resources.ResourceLinks.group;
import static sonia.scm.api.v2.resources.ResourceLinks.user;

@Mapper
public abstract class GroupToGroupDtoMapper extends BaseMapper {

  public abstract GroupDto map(Group group, @Context UriInfo uriInfo);

  @AfterMapping
  void appendLinks(Group group, @MappingTarget GroupDto target, @Context UriInfo uriInfo) {
    Links.Builder linksBuilder = linkingTo().self(group(uriInfo).self(target.getName()));
    if (GroupPermissions.delete(group).isPermitted()) {
      linksBuilder.single(link("delete", group(uriInfo).delete(target.getName())));
    }
    if (GroupPermissions.modify(group).isPermitted()) {
      linksBuilder.single(link("update", group(uriInfo).update(target.getName())));
    }
    target.add(linksBuilder.build());
  }

  @AfterMapping
  void mapMembers(Group group, @MappingTarget GroupDto target, @Context UriInfo uriInfo) {
    List<MemberDto> memberDtos = group.getMembers().stream().map(name -> this.createMember(name, uriInfo)).collect(Collectors.toList());
    target.withEmbedded("members", memberDtos);
  }

  private MemberDto createMember(String name, UriInfo uriInfo) {
    Links.Builder linksBuilder = linkingTo().self(user(uriInfo).self(name));
    MemberDto memberDto = new MemberDto(name);
    memberDto.add(linksBuilder.build());
    return memberDto;
  }
}
