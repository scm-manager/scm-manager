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
import lombok.Setter;

import java.time.Instant;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@SuppressWarnings("java:S2160") // we need no equals here
public class PluginCenterAuthenticationInfoDto extends HalRepresentation {

  @JsonInclude(NON_NULL)
  private String principal;
  @JsonInclude(NON_NULL)
  private String pluginCenterSubject;
  @JsonInclude(NON_NULL)
  private Instant date;
  private boolean isDefault;
  private boolean failed;

  public PluginCenterAuthenticationInfoDto(Links links) {
    super(links);
  }
}
