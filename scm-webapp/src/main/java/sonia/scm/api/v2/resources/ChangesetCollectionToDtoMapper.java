package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import javax.inject.Inject;

public class ChangesetCollectionToDtoMapper extends ChangesetCollectionToDtoMapperBase {

  private final ResourceLinks resourceLinks;

  @Inject
  public ChangesetCollectionToDtoMapper(ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper, ResourceLinks resourceLinks) {
    super(changesetToChangesetDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository) {
    return super.map(pageNumber, pageSize, pageResult, repository, () -> createSelfLink(repository));
  }

  private String createSelfLink(Repository repository) {
    return resourceLinks.changeset().all(repository.getNamespace(), repository.getName());
  }
}
