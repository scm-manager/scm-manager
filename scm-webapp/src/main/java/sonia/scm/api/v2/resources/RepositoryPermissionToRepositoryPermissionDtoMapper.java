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
import jakarta.inject.Inject;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespacePermissions;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermission;
import sonia.scm.repository.RepositoryPermissions;

import java.util.Optional;

import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.api.v2.resources.RepositoryPermissionDto.GROUP_PREFIX;

@Mapper
public abstract class RepositoryPermissionToRepositoryPermissionDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract RepositoryPermissionDto map(RepositoryPermission permission, @Context Repository repository);

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract RepositoryPermissionDto map(RepositoryPermission permission, @Context Namespace namespace);

  @AfterMapping
  void appendLinks(@MappingTarget RepositoryPermissionDto target, @Context Repository repository) {
    String permissionName = getUrlPermissionName(target);
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.repositoryPermission().self(repository.getNamespace(), repository.getName(), permissionName));
    if (RepositoryPermissions.permissionWrite(repository).isPermitted()) {
      linksBuilder.single(link("update", resourceLinks.repositoryPermission().update(repository.getNamespace(), repository.getName(), permissionName)));
      linksBuilder.single(link("delete", resourceLinks.repositoryPermission().delete(repository.getNamespace(), repository.getName(), permissionName)));
    }
    target.add(linksBuilder.build());
  }

  @AfterMapping
  void appendLinks(@MappingTarget RepositoryPermissionDto target, @Context Namespace namespace) {
    String permissionName = getUrlPermissionName(target);
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.namespacePermission().self(namespace.getNamespace(), permissionName));
    if (NamespacePermissions.permissionWrite().isPermitted(namespace)) {
      linksBuilder.single(link("update", resourceLinks.namespacePermission().update(namespace.getNamespace(), permissionName)));
      linksBuilder.single(link("delete", resourceLinks.namespacePermission().delete(namespace.getNamespace(), permissionName)));
    }
    target.add(linksBuilder.build());
  }

  public String getUrlPermissionName(RepositoryPermissionDto repositoryPermissionDto) {
    return Optional.of(repositoryPermissionDto.getName())
      .filter(p -> !repositoryPermissionDto.isGroupPermission())
      .orElse(GROUP_PREFIX + repositoryPermissionDto.getName());
  }
}
