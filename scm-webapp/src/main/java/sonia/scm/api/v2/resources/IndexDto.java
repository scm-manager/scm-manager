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
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;

@Getter
public class IndexDto extends HalRepresentation {

  private final String version;
  private final String instanceId;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private final String initialization;

  IndexDto(Links links, Embedded embedded, String version, String instanceId) {
    this(links, embedded, version, instanceId, null);
  }

  IndexDto(Links links, Embedded embedded, String version, String instanceId, String initialization) {
    super(links, embedded);
    this.version = version;
    this.instanceId = instanceId;
    this.initialization = initialization;
  }
}
