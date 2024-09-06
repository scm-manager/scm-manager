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

import com.google.inject.Inject;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Link;
import de.otto.edison.hal.Links;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;

import java.util.Collection;
import java.util.List;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static java.util.stream.Collectors.toList;

public class BranchCollectionToDtoMapper {

  private final ResourceLinks resourceLinks;
  private final BranchToBranchDtoMapper branchToDtoMapper;

  @Inject
  public BranchCollectionToDtoMapper(BranchToBranchDtoMapper branchToDtoMapper, ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
    this.branchToDtoMapper = branchToDtoMapper;
  }

  public HalRepresentation map(Repository repository, Collection<Branch> branches) {
    return new HalRepresentation(
      createLinks(repository),
      embedDtos(getBranchDtoList(repository, branches)));
  }

  public List<BranchDto> getBranchDtoList(Repository repository, Collection<Branch> branches) {
    return branches.stream().map(branch -> branchToDtoMapper.map(branch, repository)).collect(toList());
  }

  private Links createLinks(Repository repository) {
    String namespace = repository.getNamespace();
    String name = repository.getName();
    String baseUrl = resourceLinks.branchCollection().self(namespace, name);

    Links.Builder linksBuilder = linkingTo().with(createSelfLink(baseUrl));
    if (RepositoryPermissions.push(repository).isPermitted()) {
      linksBuilder.single(createCreateLink(namespace, name));
    }
    return linksBuilder.build();
  }

  private Links createSelfLink(String baseUrl) {
    return Links.linkingTo().self(baseUrl).build();
  }

  private Link createCreateLink(String namespace, String name) {
    return Link.link("create", resourceLinks.branch().create(namespace, name));
  }

  private Embedded embedDtos(List<BranchDto> dtos) {
    return embeddedBuilder()
      .with("branches", dtos)
      .build();
  }
}
