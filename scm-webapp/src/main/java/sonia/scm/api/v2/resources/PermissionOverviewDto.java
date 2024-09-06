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
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
@SuppressWarnings("java:S2160") // no equals needed in dto
class PermissionOverviewDto extends HalRepresentation {

  private Collection<PermissionOverviewDto.GroupEntryDto> relevantGroups;
  private Collection<String> relevantNamespaces;
  private Collection<RepositoryEntry> relevantRepositories;

  PermissionOverviewDto(Links links, Embedded embedded) {
    super(links, embedded);
  }

  @Getter
  @Setter
  static class GroupEntryDto {
    private String name;
    private boolean permissions;
    private boolean externalOnly;
  }

  @Getter
  @Setter
  static class RepositoryEntry {
    private String namespace;
    private String name;
  }
}
