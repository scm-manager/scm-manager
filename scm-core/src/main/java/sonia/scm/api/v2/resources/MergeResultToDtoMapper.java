package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import sonia.scm.repository.api.MergeCommandResult;

@Mapper
public interface MergeResultToDtoMapper {
  MergeResultDto map(MergeCommandResult result);
}
