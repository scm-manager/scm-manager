package sonia.scm.api.v2.resources;

import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
public class RepositoryCollectionToDtoMapper extends BasicCollectionToDtoMapper<Repository, RepositoryDto> {

  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryCollectionToDtoMapper(RepositoryToRepositoryDtoMapper repositoryToDtoMapper, ResourceLinks resourceLinks) {
    super("repositories", repositoryToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  @Override
  String createCreateLink() {
    return resourceLinks.repositoryCollection().create();
  }

  @Override
  String createSelfLink() {
    return resourceLinks.repositoryCollection().self();
  }

  @Override
  boolean isCreatePermitted() {
    return RepositoryPermissions.create().isPermitted();
  }
}
