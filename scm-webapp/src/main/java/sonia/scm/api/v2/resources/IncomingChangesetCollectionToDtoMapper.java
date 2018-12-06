package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import javax.inject.Inject;

public class IncomingChangesetCollectionToDtoMapper extends ChangesetCollectionToDtoMapper {


  private final ResourceLinks resourceLinks;

  @Inject
  public IncomingChangesetCollectionToDtoMapper(ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper, ResourceLinks resourceLinks) {
    super(changesetToChangesetDtoMapper, resourceLinks);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository, String source, String target) {
    return super.map(pageNumber, pageSize, pageResult, repository, () -> createSelfLink(repository, source, target));
  }

  private String createSelfLink(Repository repository, String source, String target) {
    return resourceLinks.incoming().changesets(repository.getNamespace(), repository.getName(), source, target);
  }


}
