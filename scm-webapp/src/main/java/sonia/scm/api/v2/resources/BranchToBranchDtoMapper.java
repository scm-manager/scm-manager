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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.EdisonHalAppender;

import java.time.Instant;
import java.util.Optional;

import static de.otto.edison.hal.Link.linkBuilder;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class BranchToBranchDtoMapper extends HalAppenderMapper implements InstantAttributeMapper {

  @Inject
  private ResourceLinks resourceLinks;
  @Inject
  private RepositoryServiceFactory serviceFactory;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract BranchDto map(Branch branch, @Context Repository repository);

  abstract PersonDto map(Person person);

  @ObjectFactory
  BranchDto createDto(@Context Repository repository, Branch branch) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(repository.getNamespace(), repository.getName());
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.branch().self(repository.getNamespace(), repository.getName(), branch.getName()))
      .single(linkBuilder("history", resourceLinks.branch().history(repository.getNamespace(), repository.getName(), branch.getName())).build())
      .single(linkBuilder("changeset", resourceLinks.changeset().changeset(namespaceAndName.getNamespace(), namespaceAndName.getName(), branch.getRevision())).build())
      .single(linkBuilder("source", resourceLinks.source().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), branch.getRevision())).build());

    if (!branch.isDefaultBranch() && RepositoryPermissions.push(repository).isPermitted()) {
      linksBuilder.single(linkBuilder("delete", resourceLinks.branch().delete(repository.getNamespace(), repository.getName(), branch.getName())).build());
    }

    try (RepositoryService service = serviceFactory.create(repository)) {
      if (service.isSupported(Command.BRANCH_DETAILS)) {
        linksBuilder.single(linkBuilder("details", resourceLinks.branchDetails().self(namespaceAndName.getNamespace(), namespaceAndName.getName(), branch.getName())).build());
      }
    }

    Embedded.Builder embeddedBuilder = Embedded.embeddedBuilder();
    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), branch, namespaceAndName);

    return new BranchDto(linksBuilder.build(), embeddedBuilder.build());
  }

  Instant mapOptionalTime(Optional<Long> date) {
    return date.map(this::mapTime).orElse(null);
  }
}
