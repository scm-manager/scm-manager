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

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("squid:S2160") // we do not need equals for dto
public class PluginDto extends HalRepresentation {

  private String name;
  private String version;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String newVersion;
  private String displayName;
  private String description;
  private String author;
  private String category;
  private String avatarUrl;
  private boolean pending;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean core;
  private Boolean markedForUninstall;
  private Set<String> dependencies;
  private Set<String> optionalDependencies;

  public PluginDto(Links links) {
    add(links);
  }
}
