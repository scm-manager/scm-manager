package sonia.scm.api.v2.resources;

import sonia.scm.repository.RepositoryType;

import javax.inject.Inject;

public class RepositoryTypeCollectionToDtoMapper extends CollectionToDtoMapper<RepositoryType, RepositoryTypeDto>  {

  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryTypeCollectionToDtoMapper(RepositoryTypeToRepositoryTypeDtoMapper mapper, ResourceLinks resourceLinks) {
    super("repository-types", mapper);
    this.resourceLinks = resourceLinks;
  }

  @Override
  protected String createSelfLink() {
    return resourceLinks.repositoryTypeCollection().self();
  }
}
