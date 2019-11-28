package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import javax.inject.Inject;

public class BranchChangesetCollectionToDtoMapper extends ChangesetCollectionToDtoMapperBase {

  private final ResourceLinks resourceLinks;

  @Inject
  public BranchChangesetCollectionToDtoMapper(ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper, ResourceLinks resourceLinks) {
    super(changesetToChangesetDtoMapper, resourceLinks);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository, String branch) {
    return this.map(pageNumber, pageSize, pageResult, repository, () -> createSelfLink(repository, branch), branch);
  }

  private String createSelfLink(Repository repository, String branch) {
    return resourceLinks.branch().history(repository.getNamespaceAndName(), branch);
  }
}
