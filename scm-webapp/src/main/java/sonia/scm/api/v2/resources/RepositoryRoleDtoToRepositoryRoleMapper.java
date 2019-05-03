package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sonia.scm.repository.RepositoryRole;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class RepositoryRoleDtoToRepositoryRoleMapper extends BaseDtoMapper {

  @Mapping(target = "creationDate", ignore = true)
  public abstract RepositoryRole map(RepositoryRoleDto repositoryRoleDto);
}
