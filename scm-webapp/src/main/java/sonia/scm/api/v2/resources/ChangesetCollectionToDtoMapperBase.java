/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
