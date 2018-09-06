package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sonia.scm.group.Group;

import java.time.Instant;


@Mapper
public abstract class GroupDtoToGroupMapper extends BaseDtoMapper {

  @Mapping(target = "creationDate", ignore = true)
  public abstract Group map(GroupDto groupDto);
}
