/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Links;
import sonia.scm.PageResult;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.Repository;

import java.util.Optional;
import java.util.function.Supplier;

class ChangesetCollectionToDtoMapperBase extends PagedCollectionToDtoMapper<Changeset, ChangesetDto> {

  private final ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper;
  private final ResourceLinks resourceLinks;

  ChangesetCollectionToDtoMapperBase(ChangesetToChangesetDtoMapper changesetToChangesetDtoMapper, ResourceLinks resourceLinks) {
    super("changesets");
    this.changesetToChangesetDtoMapper = changesetToChangesetDtoMapper;
    this.resourceLinks = resourceLinks;
  }

  CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository, Supplier<String> selfLinkSupplier) {
    return super.map(pageNumber, pageSize, pageResult, selfLinkSupplier.get(), Optional.empty(), changeset -> changesetToChangesetDtoMapper.map(changeset, repository));
  }

  CollectionDto map(int pageNumber, int pageSize, PageResult<Changeset> pageResult, Repository repository, Supplier<String> selfLinkSupplier, String branchName) {
    CollectionDto collectionDto = this.map(pageNumber, pageSize, pageResult, repository, selfLinkSupplier);
    collectionDto.withEmbedded("branch", createBranchReferenceDto(repository, branchName));
    return collectionDto;
  }

  private BranchReferenceDto createBranchReferenceDto(Repository repository, String branchName) {
    BranchReferenceDto branchReferenceDto = new BranchReferenceDto();
    branchReferenceDto.setName(branchName);
    branchReferenceDto.add(Links.linkingTo().self(resourceLinks.branch().self(repository.getNamespace(), repository.getName(), branchName)).build());
    return branchReferenceDto;
  }
}
