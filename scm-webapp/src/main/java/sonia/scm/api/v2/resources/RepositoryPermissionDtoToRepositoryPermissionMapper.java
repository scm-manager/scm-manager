package sonia.scm.api.v2.resources;

import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.RepositoryPermission;

@Mapper( collectionMappingStrategy = CollectionMappingStrategy.TARGET_IMMUTABLE)
public abstract class RepositoryPermissionDtoToRepositoryPermissionMapper {

  public abstract RepositoryPermission map(RepositoryPermissionDto permissionDto);
}
