package sonia.scm.api.v2.resources;

import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.util.Optional;
import java.util.function.Supplier;

class ChangesetCollectionToDtoMapperBase extends PagedCollectionToDtoMapper<Changeset, ChangesetDto> {

  private final ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper;

  ChangesetCollectionToDtoMapperBase(ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper) {
    super("changesets");
    this.changesetToChangesetDtoMapper = changesetToChangesetDtoMapper;
  }

  CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository, Supplier<String> selfLinkSupplier) {
    return super.map(pageNumber, pageSize, pageResult, selfLinkSupplier.get(), Optional.empty(), changeset -> changesetToChangesetDtoMapper.map(changeset, repository));
  }
}

