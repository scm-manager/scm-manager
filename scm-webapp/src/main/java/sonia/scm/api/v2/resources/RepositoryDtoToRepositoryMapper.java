package sonia.scm.api.v2.resources;

import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Repository;

@Mapper
public abstract class RepositoryDtoToRepositoryMapper {

  @Mapping(target = "creationDate", ignore = true)
  @Mapping(target = "lastModified", ignore = true)
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "publicReadable", ignore = true)
  @Mapping(target = "healthCheckFailures", ignore = true)
  @Mapping(target = "permissions", ignore = true)
  public abstract Repository map(RepositoryDto repositoryDto, @Context String id);

  @AfterMapping
  void updateId(@MappingTarget Repository repository, @Context String id) {
    repository.setId(id);
  }
}
