package sonia.scm.api.v2.resources;

import com.google.inject.Inject;
import org.mapstruct.Mapper;
import sonia.scm.repository.HealthCheckFailure;
import sonia.scm.repository.Permission;
import sonia.scm.repository.Repository;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
@Mapper
public abstract class RepositoryToRepositoryDtoMapper extends BaseMapper<Repository, RepositoryDto> {

  @Inject
  private ResourceLinks resourceLinks;

  abstract HealthCheckFailureDto toDto(HealthCheckFailure failure);

  abstract PermissionDto toDto(Permission permission);
}
