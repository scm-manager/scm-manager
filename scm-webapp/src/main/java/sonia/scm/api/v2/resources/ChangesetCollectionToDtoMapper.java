package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
import java.util.Optional;

public class ChangesetCollectionToDtoMapper extends BasicCollectionToDtoMapper<Changeset, ChangesetDto, ChangesetToChangesetDtoMapper> {

  private final ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper;
  private final ResourceLinks resourceLinks;

  @Inject
  public ChangesetCollectionToDtoMapper(ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper, ResourceLinks resourceLinks) {
    super("changesets", changesetToChangesetDtoMapper);
    this.changesetToChangesetDtoMapper = changesetToChangesetDtoMapper;
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository) {
    return super.map(pageNumber, pageSize, pageResult, createSelfLink(repository), Optional.empty(), changeset -> changesetToChangesetDtoMapper.map(changeset, repository));
  }

  private String createSelfLink(Repository repository) {
    return resourceLinks.changeset().all(repository.getNamespace(), repository.getName());
  }
}
