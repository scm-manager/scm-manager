package sonia.scm.api.v2.resources;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import sonia.scm.group.Group;

import java.util.stream.Collectors;

@Mapper
public abstract class GroupDto2GroupMapper {
  @Mappings({
    @Mapping(target = "creationDate", ignore = true),
    @Mapping(target = "lastModified", ignore = true)
  })
  public abstract Group groupDtoToGroup(GroupDto groupDto);

  @AfterMapping
  void mapMembers(GroupDto dto, @MappingTarget Group target) {
    target.setMembers(dto.getEmbedded().getItemsBy("members").stream().map(m -> m.getAttribute("name").asText()).collect(Collectors.toList()));
  }
}
