package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sonia.scm.group.Group;


@Mapper
public abstract class GroupDtoToGroupMapper {

  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "lastModified", ignore = true)
  public abstract Group map(GroupDto groupDto);

}
