package sonia.scm.api.v2.resources;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Permission;

@Mapper
public abstract class PermissionDtoToPermissionMapper {

  public abstract Permission map(PermissionDto permissionDto);

  /**
   * this method is needed to modify an existing permission object
   *
   * @param target the target permission
   * @param permissionDto the source dto
   * @return the mapped target permission object
   */
  public abstract Permission modify(@MappingTarget Permission target, PermissionDto permissionDto);

}
