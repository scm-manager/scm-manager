package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRolePermissions;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RepositoryRoleCollectionToDtoMapper extends BasicCollectionToDtoMapper<RepositoryRole, RepositoryRoleDto, RepositoryRoleToRepositoryRoleDtoMapper> {

  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryRoleCollectionToDtoMapper(RepositoryRoleToRepositoryRoleDtoMapper repositoryRoleToDtoMapper, ResourceLinks resourceLinks) {
    super("repositoryRoles", repositoryRoleToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<RepositoryRole> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(), this.createCreateLink());
  }

  Optional<String> createCreateLink() {
    return RepositoryRolePermissions.modify().isPermitted() ? of(resourceLinks.repositoryRoleCollection().create()): empty();
  }

  String createSelfLink() {
    return resourceLinks.repositoryRoleCollection().self();
  }
}
