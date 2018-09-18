package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import javax.inject.Inject;
import java.util.Optional;
import java.util.function.Supplier;

public class ChangesetCollectionToDtoMapper extends PagedCollectionToDtoMapper<Changeset, ChangesetDto> {

  private final ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper;
  protected final ResourceLinks resourceLinks;

  @Inject
  public ChangesetCollectionToDtoMapper(ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper, ResourceLinks resourceLinks) {
    super("changesets");
    this.changesetToChangesetDtoMapper = changesetToChangesetDtoMapper;
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository) {
    return this.map(pageNumber, pageSize, pageResult, repository, () -> createSelfLink(repository));
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository, Supplier<String> selfLinkSupplier) {
    return super.map(pageNumber, pageSize, pageResult, selfLinkSupplier.get(), Optional.empty(), changeset -> changesetToChangesetDtoMapper.map(changeset, repository));
  }

  protected String createSelfLink(Repository repository) {
    return resourceLinks.changeset().all(repository.getNamespace(), repository.getName());
  }
}

