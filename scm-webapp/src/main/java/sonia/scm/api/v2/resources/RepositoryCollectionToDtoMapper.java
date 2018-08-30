package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

// Mapstruct does not support parameterized (i.e. non-default) constructors. Thus, we need to use field injection.
@SuppressWarnings("squid:S3306")
public class RepositoryCollectionToDtoMapper extends BasicCollectionToDtoMapper<Repository, RepositoryDto, RepositoryToRepositoryDtoMapper> {

  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryCollectionToDtoMapper(RepositoryToRepositoryDtoMapper repositoryToDtoMapper, ResourceLinks resourceLinks) {
    super("repositories", repositoryToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Repository> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(), this.createCreateLink());
  }

  Optional<String> createCreateLink() {
    return RepositoryPermissions.create().isPermitted() ? of(resourceLinks.repositoryCollection().create()): empty();
  }

  String createSelfLink() {
    return resourceLinks.repositoryCollection().self();
  }
}
