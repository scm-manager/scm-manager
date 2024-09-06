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

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.BranchDetails;
import sonia.scm.repository.Repository;
import sonia.scm.web.EdisonHalAppender;

import java.util.Optional;

@Mapper
public abstract class BranchDetailsMapper extends BaseMapper<BranchDetails, BranchDetailsDto> {

  @Inject
  private ResourceLinks resourceLinks;

  abstract BranchDetailsDto map(@Context Repository repository, String branchName, BranchDetails result);

  @ObjectFactory
  BranchDetailsDto createDto(@Context Repository repository, String branchName, BranchDetails result) {
    Links.Builder linksBuilder = createLinks(repository, branchName);
    Embedded.Builder embeddedBuilder = Embedded.embeddedBuilder();

    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), result, repository);

    return new BranchDetailsDto(linksBuilder.build(), embeddedBuilder.build());
  }

  Integer map(Optional<Integer> o) {
    return o.orElse(null);
  }

  private Links.Builder createLinks(@Context Repository repository, String branch) {
    return Links.linkingTo()
      .self(
        resourceLinks.branchDetails()
          .self(
            repository.getNamespace(),
            repository.getName(),
            branch)
      );
  }

  @VisibleForTesting
  void setResourceLinks(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }
}
