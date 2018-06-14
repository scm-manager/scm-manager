package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.group.Group;
import sonia.scm.group.GroupPermissions;
import sonia.scm.util.AssertUtil;

import javax.ws.rs.core.UriInfo;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class GroupToGroupDtoMapper {

  public abstract GroupDto map(Group group, @Context UriInfo uriInfo);

  @AfterMapping
  void appendLinks(Group group, @MappingTarget GroupDto target, @Context UriInfo uriInfo) {
    LinkBuilder groupLinkBuilder = new LinkBuilder(uriInfo, GroupV2Resource.class, GroupSubResource.class);

    Links.Builder linksBuilder = linkingTo()
      .self(groupLinkBuilder.method("getGroupSubResource").parameters(target.getName()).method("get").parameters().href());
    if (GroupPermissions.delete(group).isPermitted()) {
      linksBuilder
        .single(link("delete", groupLinkBuilder.method("getGroupSubResource").parameters(target.getName()).method("delete").parameters().href()));
    }
    if (GroupPermissions.modify(group).isPermitted()) {
      linksBuilder
        .single(link("update", groupLinkBuilder.method("getGroupSubResource").parameters(target.getName()).method("update").parameters().href()));
    }
    target.add(linksBuilder.build());
  }

  @AfterMapping
  void mapMembers(Group group, @MappingTarget GroupDto target, @Context UriInfo uriInfo) {
    List<MemberDto> memberDtos = group.getMembers().stream().map(name -> this.createMember(name, uriInfo)).collect(Collectors.toList());
    target.withEmbedded("members", memberDtos);
  }

  private MemberDto createMember(String name, UriInfo uriInfo) {
    LinkBuilder userLinkBuilder = new LinkBuilder(uriInfo, UserV2Resource.class, UserSubResource.class);
    Links.Builder linksBuilder = linkingTo()
      .self(userLinkBuilder.method("getUserSubResource").parameters(name).method("get").parameters().href());
    MemberDto memberDto = new MemberDto(name);
    memberDto.add(linksBuilder.build());
    return memberDto;
  }

  @Mapping(target = "creationDate")
  Instant mapTime(Long epochMilli) {
    AssertUtil.assertIsNotNull(epochMilli);
    return Instant.ofEpochMilli(epochMilli);
  }

  @Mapping(target = "lastModified")
  Optional<Instant> mapOptionalTime(Long epochMilli) {
    return Optional
      .ofNullable(epochMilli)
      .map(Instant::ofEpochMilli);
  }
}
