package sonia.scm.api.v2.resources;

import com.fasterxml.jackson.databind.JsonNode;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.group.Group;

import java.util.stream.Collectors;

@Mapper
public abstract class GroupDtoToGroupMapper {

  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "lastModified", ignore = true)
  public abstract Group map(GroupDto groupDto);

  @AfterMapping
  void mapMembers(GroupDto dto, @MappingTarget Group target) {
    target.setMembers(
      dto
        .getEmbedded()
        .getItemsBy("members")
        .stream()
        .map(m -> m.getAttribute("name"))
        .map(JsonNode::asText)
        .collect(Collectors.toList()));
  }
}
