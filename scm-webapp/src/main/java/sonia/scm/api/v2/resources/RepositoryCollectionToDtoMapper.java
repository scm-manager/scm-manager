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

import jakarta.inject.Inject;
import sonia.scm.PageResult;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RepositoryCollectionToDtoMapper extends BasicCollectionToDtoMapper<Repository, RepositoryDto, RepositoryToRepositoryDtoMapper> {

  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryCollectionToDtoMapper(RepositoryToRepositoryDtoMapper repositoryToDtoMapper, ResourceLinks resourceLinks) {
    super("repositories", repositoryToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(String namespace, int pageNumber, int pageSize, PageResult<Repository> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(namespace), this.createCreateLink());
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<Repository> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(), this.createCreateLink());
  }

  Optional<String> createCreateLink() {
    return RepositoryPermissions.create().isPermitted() ? of(resourceLinks.repositoryCollection().create()): empty();
  }

  String createSelfLink() {
    return resourceLinks.repositoryCollection().self();
  }

  String createSelfLink(String namespace) {
    return resourceLinks.repositoryCollection().forNamespace(namespace);
  }
}
