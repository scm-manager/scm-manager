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

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import sonia.scm.repository.Namespace;
import sonia.scm.repository.NamespacePermissions;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class RepositoryPermissionCollectionToDtoMapper {

  private final ResourceLinks resourceLinks;
  private final RepositoryPermissionToRepositoryPermissionDtoMapper repositoryPermissionToRepositoryPermissionDtoMapper;

  @Inject
  public RepositoryPermissionCollectionToDtoMapper(RepositoryPermissionToRepositoryPermissionDtoMapper repositoryPermissionToRepositoryPermissionDtoMapper, ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
    this.repositoryPermissionToRepositoryPermissionDtoMapper = repositoryPermissionToRepositoryPermissionDtoMapper;
  }

  public HalRepresentation map(Repository repository) {
    List<RepositoryPermissionDto> repositoryPermissionDtoList = repository.getPermissions()
      .stream()
      .map(permission -> repositoryPermissionToRepositoryPermissionDtoMapper.map(permission, repository))
      .collect(toList());
    return new HalRepresentation(createLinks(repository), embedDtos(repositoryPermissionDtoList));
  }

  public HalRepresentation map(Namespace namespace) {
    List<RepositoryPermissionDto> repositoryPermissionDtoList = namespace.getPermissions()
      .stream()
      .map(permission -> repositoryPermissionToRepositoryPermissionDtoMapper.map(permission, namespace))
      .collect(toList());
    return new HalRepresentation(createLinks(namespace), embedDtos(repositoryPermissionDtoList));
  }

  private Links createLinks(Repository repository) {
    RepositoryPermissions.permissionRead(repository).check();
    Links.Builder linksBuilder = linkingTo()
      .with(Links.linkingTo().self(resourceLinks.repositoryPermission().all(repository.getNamespace(), repository.getName())).build());
    if (RepositoryPermissions.permissionWrite(repository).isPermitted()) {
      linksBuilder.single(link("create", resourceLinks.repositoryPermission().create(repository.getNamespace(), repository.getName())));
    }
    return linksBuilder.build();
  }

  private Links createLinks(Namespace namespace) {
    NamespacePermissions.permissionRead().check(namespace);
    Links.Builder linksBuilder = linkingTo()
      .with(Links.linkingTo().self(resourceLinks.namespacePermission().all(namespace.getNamespace())).build());
    if (NamespacePermissions.permissionWrite().isPermitted(namespace)) {
      linksBuilder.single(link("create", resourceLinks.namespacePermission().create(namespace.getNamespace())));
    }
    return linksBuilder.build();
  }

  private Embedded embedDtos(List<RepositoryPermissionDto> repositoryPermissionDtoList) {
    return embeddedBuilder()
      .with("permissions", repositoryPermissionDtoList)
      .build();
  }
}
