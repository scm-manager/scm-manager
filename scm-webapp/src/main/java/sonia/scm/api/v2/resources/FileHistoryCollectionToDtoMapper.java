package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import javax.inject.Inject;

public class FileHistoryCollectionToDtoMapper extends ChangesetCollectionToDtoMapper {


  @Inject
  public FileHistoryCollectionToDtoMapper(ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper, ResourceLinks resourceLinks) {
    super(changesetToChangesetDtoMapper, resourceLinks);
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository, String revision, String path) {
    return super.map(pageNumber, pageSize, pageResult, repository, () -> createSelfLink(repository, revision, path));
  }

  protected String createSelfLink(Repository repository, String revision, String path) {
    return super.resourceLinks.fileHistory().self(repository.getNamespace(), repository.getName(), revision, path);
  }
}
