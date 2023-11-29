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
import sonia.scm.repository.RepositoryRole;
import sonia.scm.repository.RepositoryRolePermissions;

import java.util.Optional;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class RepositoryRoleCollectionToDtoMapper extends BasicCollectionToDtoMapper<RepositoryRole, RepositoryRoleDto, RepositoryRoleToRepositoryRoleDtoMapper> {

  private final ResourceLinks resourceLinks;

  @Inject
  public RepositoryRoleCollectionToDtoMapper(RepositoryRoleToRepositoryRoleDtoMapper repositoryRoleToDtoMapper, ResourceLinks resourceLinks) {
    super("repositoryRoles", repositoryRoleToDtoMapper);
    this.resourceLinks = resourceLinks;
  }

  public CollectionDto map(int pageNumber, int pageSize, PageResult<RepositoryRole> pageResult) {
    return map(pageNumber, pageSize, pageResult, this.createSelfLink(), this.createCreateLink());
  }

  Optional<String> createCreateLink() {
    return RepositoryRolePermissions.write().isPermitted() ? of(resourceLinks.repositoryRoleCollection().create()): empty();
  }

  String createSelfLink() {
    return resourceLinks.repositoryRoleCollection().self();
  }
}
